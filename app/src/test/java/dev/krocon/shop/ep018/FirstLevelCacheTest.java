package dev.krocon.shop.ep018;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Product;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Folge 018: First-Level-Cache = Persistence Context, lebt nur eine Transaktion lang. */
@ShopJpaTest
@Import(ProductCatalogService.class)
class FirstLevelCacheTest {

  @Autowired ProductCatalogService catalog;
  @Autowired EntityManager em;
  Statistics stats;

  @BeforeEach
  void resetStats() {
    stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    stats.clear();
  }

  @Test
  void sameTransaction_secondFind_noQuery_sameObject() {
    Product a = em.find(Product.class, 1L);   // SELECT
    Product b = em.find(Product.class, 1L);   // aus dem Persistence Context

    assertThat(b).isSameAs(a);
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)   // jeder show()-Aufruf = eigener Request
  void everyRequest_newSession_selectAgain() {
    catalog.show(1L);   // 2× find, 1 SELECT
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);

    catalog.show(1L);   // nächster Request, neue Session → wieder SELECT
    catalog.show(1L);
    assertThat(stats.getPrepareStatementCount()).isEqualTo(3);
  }
}
