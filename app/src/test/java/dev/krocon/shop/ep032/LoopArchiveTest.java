package dev.krocon.shop.ep032;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import dev.krocon.shop.ep034.QueryCount;
import dev.krocon.shop.ep034.SqlRecorder;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

/**
 * Folge 032: Schleife mit Dirty Checking (N UPDATEs) vs. ein Bulk-UPDATE.
 * Hinweis: Das Demo-Enum kennt kein ARCHIVED – wir "archivieren" mit CANCELLED.
 */
@ShopJpaTest
@TestPropertySource(properties = SqlRecorder.PROPERTY)
class LoopArchiveTest {

  static final Instant CUTOFF = Instant.parse("2025-07-01T00:00:00Z");

  @Autowired Ep032OrderRepository orderRepository;
  @Autowired EntityManager em;

  @Test
  void loopWithDirtyChecking_sendsOneUpdatePerRow() {
    QueryCount queries = QueryCount.of(em);
    Statistics stats = queries.statistics();

    long statements = queries.count(() -> {
      List<Order> old = orderRepository.findByCreatedAtBefore(CUTOFF);
      for (Order o : old) {
        o.setStatus(OrderStatus.CANCELLED);   // Dirty Checking
      }
      em.flush();                              // hier feuern die UPDATEs
    });

    System.out.printf("ep032 loop: %d Entities geladen, %d UPDATEs, %d Statements%n",
      stats.getEntityLoadCount(), stats.getEntityUpdateCount(), statements);
    assertThat(stats.getEntityLoadCount()).isEqualTo(329);   // alle im Persistence Context
    assertThat(stats.getEntityUpdateCount()).isEqualTo(263); // 329 minus 66 schon CANCELLED
    assertThat(statements).isEqualTo(1 + 263);               // 1 SELECT + N UPDATEs
  }

  @Test
  void bulkUpdate_sendsOneStatement() {
    QueryCount queries = QueryCount.of(em);
    SqlRecorder.clear();

    QueryCount.Counted<Integer> counted =
      queries.measure(() -> orderRepository.archiveOlderThan(OrderStatus.CANCELLED, CUTOFF));

    System.out.println("ep032 bulk: " + SqlRecorder.statements() + " → " + counted.result() + " Zeilen");
    assertThat(counted.statements()).isEqualTo(1);
    assertThat(counted.result()).isEqualTo(329);              // Rückgabe = Anzahl Zeilen
    assertThat(queries.statistics().getEntityLoadCount()).isZero();
    assertThat(SqlRecorder.statements().get(0))
      .isEqualTo("update orders o1_0 set status=? where o1_0.created_at<?");
  }
}
