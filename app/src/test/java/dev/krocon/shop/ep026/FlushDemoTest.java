package dev.krocon.shop.ep026;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Customer;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** Folge 026: Flush schickt SQL, die Transaktion (hier: Test-Rollback) läuft weiter. */
@ShopJpaTest
class FlushDemoTest {

  @Autowired EntityManager em;
  @Autowired JdbcTemplate jdbc;
  Statistics stats;

  @BeforeEach
  void setUp() {
    stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    stats.clear();
  }

  @Test
  void sequence_insertOnlyAtFlush() {
    Ep026Customer c = new Ep026Customer("flush@shop.de", "Flush");
    em.persist(c);  // bei SEQUENCE: noch kein INSERT
    assertThat(stats.getEntityInsertCount()).isZero();
    assertThat(countByEmail("flush@shop.de")).isZero();      // DB sieht noch nichts

    em.flush();     // jetzt: INSERT an die DB, noch rollbackbar
    assertThat(stats.getEntityInsertCount()).isEqualTo(1);
    assertThat(countByEmail("flush@shop.de")).isEqualTo(1);  // gleiche Transaktion sieht die Zeile
  }

  @Test
  void identity_insertAlreadyAtPersist() {
    Customer c = new Customer("identity@shop.de", "Identity", "DE");   // domain.Customer: IDENTITY
    em.persist(c);  // INSERT sofort, sonst gäbe es keine ID
    assertThat(stats.getEntityInsertCount()).isEqualTo(1);
  }

  @Test
  void flushModeAuto_flushesBeforeAffectedJpqlQuery() {
    em.persist(new Ep026Customer("auto@shop.de", "Auto"));
    assertThat(stats.getEntityInsertCount()).isZero();

    // JPQL auf Ep026Customer → Hibernate flusht vorher automatisch
    Long n = em.createQuery("select count(c) from Ep026Customer c where c.email = 'auto@shop.de'", Long.class)
        .getSingleResult();
    assertThat(n).isEqualTo(1);
    assertThat(stats.getEntityInsertCount()).isEqualTo(1);
  }

  @Test
  void flushModeAuto_notBeforeUnrelatedJpqlQuery() {
    em.persist(new Ep026Customer("other@shop.de", "Other"));
    // Query betrifft eine andere Tabelle → kein Flush nötig
    em.createQuery("select count(p) from Product p", Long.class).getSingleResult();
    assertThat(stats.getEntityInsertCount()).isZero();
  }

  @Test
  void nativeQuery_flushBehaviour() {
    em.persist(new Ep026Customer("native@shop.de", "Native"));
    // Native SQL: Hibernate kennt die betroffenen Tabellen nicht genau
    Number n = (Number) em.createNativeQuery("select count(*) from ep026_customers where email = 'native@shop.de'")
        .getSingleResult();
    System.out.println("Native Query ohne expliziten flush(): count = " + n
        + ", Inserts = " + stats.getEntityInsertCount());
    assertThat(n.longValue()).isEqualTo(1);   // Hibernate 7 flusht vor nativen Queries (FlushMode AUTO)
  }

  @Test
  void jdbcBypassesHibernate_needsExplicitFlush() {
    em.persist(new Ep026Customer("jdbc@shop.de", "Jdbc"));
    assertThat(countByEmail("jdbc@shop.de")).isZero();   // JdbcTemplate löst keinen Flush aus
    em.flush();
    assertThat(countByEmail("jdbc@shop.de")).isEqualTo(1);
  }

  private long countByEmail(String email) {
    return jdbc.queryForObject("select count(*) from ep026_customers where email = ?", Long.class, email);
  }
}
