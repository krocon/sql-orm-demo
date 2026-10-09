package dev.krocon.shop.ep024;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Folge 024 (Hook): Identity Map und First-Level-Cache. */
@ShopJpaTest   // = @Transactional: ein Persistence Context pro Test
class IdentityMapTest {

  @Autowired EntityManager em;
  Statistics stats;

  @BeforeEach
  void setUp() {
    stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    stats.clear();
  }

  @Test
  void findTwice_oneSelect_sameObject() {
    Long id = 1L;
    Order a = em.find(Order.class, id);  // SELECT
    Order b = em.find(Order.class, id);  // kein SQL: schon managed
    assert a == b;                       // dasselbe Objekt

    assertThat(a).isSameAs(b);
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
  }

  @Test
  void clearEmptiesTheContext() {
    Order a = em.find(Order.class, 1L);
    em.clear();                          // Context leeren → a ist detached
    Order c = em.find(Order.class, 1L);  // neues SELECT, neues Objekt

    assertThat(em.contains(a)).isFalse();
    assertThat(c).isNotSameAs(a);
    assertThat(stats.getPrepareStatementCount()).isEqualTo(2);
  }

  @Test
  void queryResultsAreAlsoDeduplicated() {
    Order a = em.find(Order.class, 1L);
    // JPQL läuft immer gegen die DB, liefert aber das bereits managed Objekt zurück
    Order viaQuery = em.createQuery("select o from Order o where o.id = 1", Order.class).getSingleResult();
    assertThat(viaQuery).isSameAs(a);
    assertThat(stats.getPrepareStatementCount()).isEqualTo(2);
  }
}
