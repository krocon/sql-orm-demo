package dev.krocon.shop.ep028;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/** Folge 028: abgeleitete Queries, @Query, Projections, Paging – gegen echte PostgreSQL. */
@ShopJpaTest
class OrderRepositoryTest {

  @Autowired Ep028OrderRepository orderRepository;
  @Autowired EntityManager em;
  Statistics stats;

  @BeforeEach
  void setUp() {
    stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    stats.clear();
  }

  @Test
  void derivedQuery() {
    List<Order> orders = orderRepository.findByCustomerEmailAndStatus("kunde1@shop.example", OrderStatus.PAID);
    assertThat(orders).isNotEmpty()
        .allMatch(o -> o.getStatus() == OrderStatus.PAID)
        .allMatch(o -> o.getCustomer().getEmail().equals("kunde1@shop.example"));
  }

  @Test
  void queryWithPaging() {
    Page<Order> page = orderRepository.findLarge(new BigDecimal("300"), PageRequest.of(0, 20));
    System.out.println("findLarge: Seite 1 von " + page.getTotalPages() + ", " + page.getNumberOfElements()
        + " von " + page.getTotalElements() + " Bestellungen, " + stats.getPrepareStatementCount() + " Queries");
    assertThat(page.getContent()).hasSize(20);
    assertThat(page.getTotalElements()).isGreaterThan(20);
    assertThat(stats.getPrepareStatementCount()).isEqualTo(2);   // Seite (limit/offset) + count
  }

  @Test
  void recordProjection() {
    List<OrderSummary> list = orderRepository.findByStatus(OrderStatus.SHIPPED);
    System.out.println("findByStatus → " + list.size() + " OrderSummary, z. B. " + list.get(0));
    assertThat(list).hasSize(1000);
    assertThat(list.get(0).customerEmail()).endsWith("@shop.example");
  }

  @Test
  void interfaceProjection() {
    List<OrderTotalView> top = orderRepository.findTop3ByStatusOrderByTotalDesc(OrderStatus.PAID);
    assertThat(top).hasSize(3);
    assertThat(top.get(0).getTotal()).isGreaterThanOrEqualTo(top.get(1).getTotal());
  }

  @Test
  void tooLongMethodName_stillWorks() {
    List<Order> list = orderRepository.findByCustomerEmailAndStatusInAndCreatedAtAfterAndTotalGreaterThan(
        "kunde1@shop.example", List.of(OrderStatus.PAID, OrderStatus.SHIPPED), Instant.parse("2025-01-01T00:00:00Z"),
        BigDecimal.ZERO);
    assertThat(list).allMatch(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.SHIPPED);
  }
}
