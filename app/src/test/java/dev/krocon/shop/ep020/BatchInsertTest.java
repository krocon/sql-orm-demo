package dev.krocon.shop.ep020;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

/** Folge 020: 1000 Bestellungen importieren – SEQUENCE bündelt, IDENTITY nicht. */
@ShopJpaTest
@TestPropertySource(properties = "spring.jpa.properties.hibernate.jdbc.batch_size=50")
class BatchInsertTest {

  static final int ROWS = 1000;

  @Autowired EntityManager em;
  Statistics stats;

  @BeforeEach
  void setUp() {
    stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    stats.clear();
  }

  @Test
  void identityInsertsImmediatelyOnPersist() {
    Ep020IdentityOrder order = new Ep020IdentityOrder(1L, new BigDecimal("9.99"));
    em.persist(order);
    // INSERT ist schon raus – sonst gäbe es keine ID
    assertThat(order.getId()).isNotNull();
    assertThat(stats.getEntityInsertCount()).isEqualTo(1);
  }

  @Test
  void sequenceInsertsOnFlush() {
    Ep020SequenceOrder order = new Ep020SequenceOrder(1L, new BigDecimal("9.99"));
    em.persist(order);
    // ID aus der Sequenz ist da, INSERT aber noch nicht
    assertThat(order.getId()).isNotNull();
    assertThat(stats.getEntityInsertCount()).isZero();
    em.flush();
    assertThat(stats.getEntityInsertCount()).isEqualTo(1);
  }

  @Test
  void identityCannotBatch() {
    for (int i = 0; i < ROWS; i++) {
      em.persist(new Ep020IdentityOrder(1L, BigDecimal.TEN));
    }
    em.flush();
    long statements = stats.getPrepareStatementCount();
    System.out.println("IDENTITY: " + ROWS + " Zeilen → " + statements + " JDBC-Statements");
    assertThat(statements).isEqualTo(ROWS);            // jedes INSERT einzeln
  }

  @Test
  void sequenceBatchesInserts() {
    for (int i = 0; i < ROWS; i++) {
      em.persist(new Ep020SequenceOrder(1L, BigDecimal.TEN));
    }
    em.flush();
    long statements = stats.getPrepareStatementCount();
    System.out.println("SEQUENCE: " + ROWS + " Zeilen → " + statements + " JDBC-Statements"
        + " (Sequenz-Aufrufe + INSERT-Batches à 50)");
    // ~20 Sequenz-Aufrufe (allocationSize 50) + 20 Batches statt 1000 einzelner INSERTs
    assertThat(statements).isLessThanOrEqualTo(45);
    assertThat(stats.getEntityInsertCount()).isEqualTo(ROWS);
  }

  @Test
  void uuidIsGeneratedInJava() {
    Ep020UuidOrder order = new Ep020UuidOrder(1L, BigDecimal.ONE);
    assertThat(order.getId()).isNull();
    em.persist(order);
    assertThat(order.getId()).isNotNull();             // ohne DB-Roundtrip
    assertThat(stats.getPrepareStatementCount()).isZero();
    em.flush();
    assertThat(stats.getEntityInsertCount()).isEqualTo(1);
  }

  @Test
  void equalsHashCodeSurviveIdAssignment() {
    Ep020SequenceOrder order = new Ep020SequenceOrder(1L, BigDecimal.ONE);
    Set<Ep020SequenceOrder> set = new HashSet<>();
    set.add(order);                 // id == null
    em.persist(order);              // jetzt hat sie eine ID
    assertThat(set).contains(order);   // hashCode konstant → wird noch gefunden
    // zwei neue Objekte ohne ID sind nicht gleich
    assertThat(new Ep020SequenceOrder(1L, BigDecimal.ONE)).isNotEqualTo(new Ep020SequenceOrder(1L, BigDecimal.ONE));
  }
}
