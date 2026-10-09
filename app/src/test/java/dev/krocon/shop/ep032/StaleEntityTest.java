package dev.krocon.shop.ep032;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Folge 032: Bulk-UPDATE umgeht den Persistence Context.
 * Ohne clearAutomatically liefert die schon geladene Entity den alten Status.
 */
@ShopJpaTest
class StaleEntityTest {

  static final Instant CUTOFF = Instant.parse("2025-07-01T00:00:00Z");
  static final long ORDER_ID = 5L;   // NEW, vom 06.06.2025

  @Autowired Ep032OrderRepository orderRepository;
  @Autowired EntityManager em;

  @Test
  void withoutClear_loadedEntityIsStale() {
    Order order = orderRepository.findById(ORDER_ID).orElseThrow();
    assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);

    orderRepository.archiveOlderThanWithoutClear(OrderStatus.CANCELLED, CUTOFF);

    // In der DB steht CANCELLED …
    String inDb = (String) em.createNativeQuery("select status from orders where id = 5").getSingleResult();
    assertThat(inDb).isEqualTo("CANCELLED");
    // … aber der Persistence Context liefert weiter den alten Wert – auch bei findById
    assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
    assertThat(orderRepository.findById(ORDER_ID).orElseThrow().getStatus()).isEqualTo(OrderStatus.NEW);
  }

  @Test
  void withClearAutomatically_reloadSeesNewValue() {
    Order order = orderRepository.findById(ORDER_ID).orElseThrow();

    orderRepository.archiveOlderThan(OrderStatus.CANCELLED, CUTOFF);   // clear + flush

    assertThat(em.contains(order)).isFalse();                           // Kontext geleert
    assertThat(orderRepository.findById(ORDER_ID).orElseThrow().getStatus())
      .isEqualTo(OrderStatus.CANCELLED);                                 // frisch geladen
  }

  @Test
  void flushAutomatically_keepsPendingChanges() {
    Order order = orderRepository.findById(ORDER_ID).orElseThrow();
    order.setTotal(new BigDecimal("99.99"));   // noch nicht geflusht

    orderRepository.archiveOlderThan(OrderStatus.CANCELLED, CUTOFF);   // flush vor dem UPDATE, dann clear

    Order reloaded = orderRepository.findById(ORDER_ID).orElseThrow();
    assertThat(reloaded.getTotal()).isEqualByComparingTo("99.99");      // Änderung ist drin
    assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.CANCELLED);
  }

  /**
   * Gemessen mit Hibernate 7.4: Auch OHNE flushAutomatically ist die Änderung in der DB,
   * weil Hibernate im FlushMode AUTO vor jedem JPQL-UPDATE auf dieselbe Tabelle selbst flusht.
   * flushAutomatically macht das nur explizit (und gilt auch bei FlushMode COMMIT).
   */
  @Test
  void clearWithoutFlush_hibernateAutoFlushStillWrites() {
    Order order = orderRepository.findById(ORDER_ID).orElseThrow();
    order.setTotal(new BigDecimal("99.99"));

    orderRepository.archiveOlderThanWithoutFlush(OrderStatus.CANCELLED, CUTOFF);

    BigDecimal total = (BigDecimal) em.createNativeQuery("select total from orders where id = 5").getSingleResult();
    System.out.println("ep032 clear ohne flushAutomatically: total in DB = " + total);
    assertThat(total).isEqualByComparingTo("99.99");
  }
}
