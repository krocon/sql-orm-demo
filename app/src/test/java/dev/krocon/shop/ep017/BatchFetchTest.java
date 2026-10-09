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
import org.springframework.test.context.TestPropertySource;

/** Folge 017, Lösung 3: Batch Fetching – fehlende Kunden gesammelt nachladen. */
@ShopJpaTest
@Import(OrderListService.class)
@TestPropertySource(properties = "spring.jpa.properties.hibernate.default_batch_fetch_size=50")
class BatchFetchTest {

  @Autowired OrderListService service;
  @Autowired Ep017OrderRepository orderRepository;
  @Autowired EntityManager em;
  Statistics stats;

  @BeforeEach
  void resetStats() {
    stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    stats.clear();
  }

  @Test
  void sameLoop_100Orders_3Queries() {
    List<String> names = service.customerNames(NEW, 100);   // unveränderter Problem-Code

    assertThat(names).hasSize(100);
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1 + 2);   // 100 Kunden / 50 je Batch
  }

  @Test
  void allNewOrders_21Queries() {
    List<Order> orders = orderRepository.findByStatus(NEW);
    orders.forEach(o -> o.getCustomer().getName());

    System.out.printf("Batch Fetching: %d Bestellungen → %d Queries%n",
        orders.size(), stats.getPrepareStatementCount());
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1 + 20);  // 999 Kunden / 50 → 20 Batches
  }
}
