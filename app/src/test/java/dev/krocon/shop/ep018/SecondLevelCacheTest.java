package dev.krocon.shop.ep018;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** Folge 018: Second-Level-Cache über Sessions hinweg – und seine Invalidierungs-Falle. */
@ShopJpaTest
@Import(ProductCatalogService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)   // jede Transaktion = eigener Request
@TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.cache.use_second_level_cache=true",
    "spring.jpa.properties.hibernate.cache.use_query_cache=true",
    // Demo-Provider ohne Dependency; produktiv: ...region.factory_class=jcache (+ Ehcache)
    "spring.jpa.properties.hibernate.cache.region.factory_class=dev.krocon.shop.ep018.MapRegionFactory"
})
class SecondLevelCacheTest {

  @Autowired ProductCatalogService catalog;
  @Autowired EntityManagerFactory emf;
  @Autowired EntityManager em;
  @Autowired TransactionTemplate tx;
  @Autowired DataSource dataSource;
  Statistics stats;
  BigDecimal originalPrice;

  @BeforeEach
  void setUp() {
    emf.getCache().evictAll();
    stats = emf.unwrap(SessionFactory.class).getStatistics();
    originalPrice = JdbcClient.create(dataSource)
      .sql("SELECT price FROM products WHERE id = 1").query(BigDecimal.class).single();
    stats.clear();
  }

  @AfterEach
  void restore() {
    JdbcClient.create(dataSource).sql("UPDATE products SET price = ? WHERE id = 1")
      .param(originalPrice).update();
    emf.getCache().evictAll();
  }

  @Test
  void secondRequest_comesFromSecondLevelCache() {
    catalog.showCached(1L);   // Request 1: SELECT, Ergebnis landet im L2-Cache
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    assertThat(stats.getSecondLevelCachePutCount()).isEqualTo(1);

    catalog.showCached(1L);   // Request 2: neue Session, aber kein SELECT
    catalog.showCached(1L);   // Request 3: dito
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    assertThat(stats.getSecondLevelCacheHitCount()).isEqualTo(2);
  }

  @Test
  void queryCache_mustBeEnabledPerQuery() {
    for (int i = 0; i < 2; i++) {
      tx.executeWithoutResult(s -> em.createQuery(
              "select p from Ep018Product p where p.sku = :sku", CachedProduct.class)
          .setParameter("sku", "MUG-1")
          .setHint("org.hibernate.cacheable", true)   // Query Cache pro Query einschalten
          .getSingleResult());
    }
    assertThat(stats.getQueryCacheHitCount()).isEqualTo(1);
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);   // 2. Lauf: Query- + Entity-Cache
  }

  @Test
  void staleData_whenTableIsChangedBehindHibernatesBack() {
    BigDecimal cached = catalog.showCached(1L).getPrice();   // füllt den Cache

    // anderer Dienst ändert den Preis direkt in der DB – Hibernate merkt nichts
    JdbcClient.create(dataSource).sql("UPDATE products SET price = price + 10 WHERE id = 1").update();

    assertThat(catalog.showCached(1L).getPrice()).isEqualByComparingTo(cached);   // veraltet!

    emf.getCache().evict(CachedProduct.class, 1L);           // manuell invalidieren
    assertThat(catalog.showCached(1L).getPrice())
      .isEqualByComparingTo(cached.add(BigDecimal.TEN));     // jetzt aktuell
  }
}
