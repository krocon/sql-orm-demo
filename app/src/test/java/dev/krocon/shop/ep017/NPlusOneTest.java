package dev.krocon.shop.ep017;

import static dev.krocon.shop.domain.OrderStatus.NEW;
import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/** Folge 017: Queries zählen – N+1 erkennen und beheben. */
@ShopJpaTest
@Import(OrderListService.class)
class NPlusOneTest {

  @Autowired OrderListService service;
  @Autowired Ep017OrderRepository orderRepository;
  @Autowired EntityManager em;
  Statistics stats;

  @BeforeEach
  void resetStats() {
    stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    stats.clear();
  }

  private long queries() {
    return stats.getPrepareStatementCount();
  }

  @Test
  void problem_100Orders_101Queries() {
    List<String> names = service.customerNames(NEW, 100);

    assertThat(names).hasSize(100);
    assertThat(queries()).isEqualTo(101);   // 1 + 100 (jede Bestellung hat hier einen anderen Kunden)
  }

  @Test
  void problem_allNewOrders() {
    List<Order> orders = orderRepository.findByStatus(NEW);   // 1 Query
    orders.forEach(o -> o.getCustomer().getName());          // + 1 pro Kunde

    System.out.printf("N+1: %d Bestellungen → %d Queries%n", orders.size(), queries());
    assertThat(orders).hasSize(1000);
    assertThat(queries()).isEqualTo(1 + 999);   // 999 verschiedene Kunden
  }

  @Test
  void fix_entityGraph_oneQuery() {
    List<String> names = service.customerNamesWithGraph(NEW, 100);

    assertThat(names).hasSize(100);
    assertThat(queries()).isEqualTo(1);
  }

  @Test
  void fix_joinFetch_oneQuery() {
    List<Order> orders = orderRepository.findJoinFetchByStatus(NEW);
    orders.forEach(o -> o.getCustomer().getName());

    assertThat(orders).hasSize(1000);
    assertThat(queries()).isEqualTo(1);
  }

  @Test
  void fix_dtoProjection_oneQuery_noEntities() {
    List<OrderRow> rows = orderRepository.findRowsByStatus(NEW);

    assertThat(rows).hasSize(1000).allSatisfy(r -> assertThat(r.customerName()).isNotBlank());
    assertThat(queries()).isEqualTo(1);
    assertThat(stats.getEntityLoadCount()).isZero();   // nur Records, nichts im Persistence Context
  }
}
