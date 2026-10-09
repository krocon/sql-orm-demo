package dev.krocon.shop.ep016;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Folge 016: Lazy braucht eine offene Session (open-in-view: false). */
@ShopJpaTest
@Import(OrderService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)   // keine Test-Transaktion, die die Session offen hält
class LazyInitTest {

  @Autowired OrderService orderService;
  @Autowired Ep016OrderRepository orders;

  @Test
  void problem_accessAfterTransaction_throws() {
    Order order = orderService.find(1001L);   // Transaktion vorbei

    assertThatThrownBy(() -> order.getCustomer().getName())
      .isInstanceOf(LazyInitializationException.class)
      .hasMessageContaining("Could not initialize proxy")
      .hasMessageContaining("no session")
      .satisfies(e -> System.out.println("LazyInitializationException: " + e.getMessage()));
  }

  @Test
  void solution_joinFetch() {
    Order order = orderService.findWithCustomer(1001L);
    assertThat(order.getCustomer().getName()).isNotBlank();
  }

  @Test
  void solution_entityGraph() {
    Order order = orders.findWithCustomerGraphById(1001L).orElseThrow();
    assertThat(order.getCustomer().getName()).isNotBlank();
  }
}
