package dev.krocon.shop.ep013;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/** Folge 013: gleiches Ergebnis, sehr unterschiedlicher Aufwand. */
@ShopJpaTest
@Import(RevenueService.class)
class RevenueServiceTest {

  @Autowired RevenueService service;
  @Autowired EntityManager em;
  Statistics stats;

  @BeforeEach
  void resetStats() {
    stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    stats.clear();
  }

  @Test
  void entityLoop_loadsEverything_andFiresOneQueryPerCustomer() {
    Map<String, BigDecimal> revenue = service.revenueWithEntities();

    System.out.printf("Entity-Schleife: %d Queries, %d Entities geladen%n",
        stats.getPrepareStatementCount(), stats.getEntityLoadCount());
    assertThat(revenue).hasSize(5);
    assertThat(stats.getEntityLoadCount()).isGreaterThanOrEqualTo(5000 + 999);   // alle Orders + Kunden
    assertThat(stats.getPrepareStatementCount()).isGreaterThan(900);             // 1 + eine pro Kunde
  }

  @Test
  void nativeSql_oneQuery_sameNumbers_plusGrandTotal() {
    Map<String, BigDecimal> expected = service.revenueWithEntities();
    em.clear();
    stats.clear();

    List<Revenue> report = service.revenueWithSql();

    System.out.printf("SQL mit ROLLUP: %d Query, %s%n", stats.getPrepareStatementCount(), report);
    assertThat(stats.getPrepareStatementCount()).isZero();   // JdbcClient läuft an Hibernate vorbei
    assertThat(stats.getEntityLoadCount()).isZero();         // keine einzige Entity im Speicher
    assertThat(report).hasSize(6);                           // 5 Länder + Gesamtsumme
    for (Revenue r : report.subList(0, 5)) {
      assertThat(r.total()).isEqualByComparingTo(expected.get(r.country()));
    }
    Revenue grandTotal = report.get(5);
    assertThat(grandTotal.country()).isNull();               // ROLLUP-Zeile
    assertThat(grandTotal.total()).isEqualByComparingTo(
        expected.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
  }
}
