package dev.krocon.shop.ep014;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Customer;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Folge 014: NEW → MANAGED → DETACHED → (merge) MANAGED → REMOVED. */
@ShopJpaTest
class LifecycleTest {

  @Autowired EntityManager em;
  Statistics stats;

  @BeforeEach
  void resetStats() {
    stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    stats.clear();
  }

  @Test
  void lifecycle() {
    Customer c = new Customer("ada@shop.de", "A.", "DE");   // NEW
    assertThat(state(c)).isEqualTo("NEW");

    em.persist(c);                                                   // MANAGED
    assertThat(state(c)).isEqualTo("MANAGED");
    assertThat(stats.getEntityInsertCount()).isEqualTo(1);   // IDENTITY: INSERT sofort, für die ID

    c.setName("Ada");                                        // Flush schreibt mit …
    em.flush();
    assertThat(stats.getEntityUpdateCount()).isEqualTo(1);

    em.detach(c);                                            // DETACHED
    assertThat(state(c)).isEqualTo("DETACHED");
    c.setName("Ada L.");                                     // … aber detached schaut keiner zu
    em.flush();
    assertThat(stats.getEntityUpdateCount()).isEqualTo(1);  // kein weiteres UPDATE

    Customer m = em.merge(c);                                // m MANAGED, c nicht!
    assertThat(m).isNotSameAs(c);
    assertThat(state(m)).isEqualTo("MANAGED");
    assertThat(state(c)).isEqualTo("DETACHED");
    assertThat(m.getName()).isEqualTo("Ada L.");             // Zustand von c kopiert

    em.remove(m);                                            // REMOVED
    assertThat(state(m)).isEqualTo("REMOVED");
    assertThat(stats.getEntityDeleteCount()).isZero();       // DELETE erst beim Flush
    em.flush();
    assertThat(stats.getEntityDeleteCount()).isEqualTo(1);
  }

  @Test
  void withoutFlushBeforeDetach_mergeFailsInHibernate741() {
    // Genau die Reihenfolge ohne flush(): persist → detach → merge.
    // Hibernate 7.4.1 wirft hier eine NPE (Customer hat die Collection orders).
    Customer c = new Customer("ada@shop.de", "A.", "DE");
    em.persist(c);
    c.setName("Ada");
    em.detach(c);
    assertThatThrownBy(() -> em.merge(c)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void mergeTrap_changesOnTheOriginalAreLost() {
    Customer c = em.find(Customer.class, 42L);
    String oldName = c.getName();
    em.detach(c);

    Customer m = em.merge(c);   // Rückgabewert ist die managed Instanz
    c.setName("falsch");        // Fehler: Änderung am Original (detached)
    em.flush();
    em.clear();

    assertThat(em.find(Customer.class, 42L).getName()).isEqualTo(oldName);   // nichts gespeichert
    assertThat(m).isNotSameAs(c);
  }

  /** Zustand über den Persistence Context bestimmen (Hibernate-SPI, nur für die Demo). */
  private String state(Customer entity) {
    if (em.contains(entity)) {
      return "MANAGED";
    }
    EntityEntry entry = em.unwrap(SessionImplementor.class).getPersistenceContext().getEntry(entity);
    if (entry != null && entry.getStatus() == Status.DELETED) {
      return "REMOVED";
    }
    return entity.getId() == null ? "NEW" : "DETACHED";
  }
}
