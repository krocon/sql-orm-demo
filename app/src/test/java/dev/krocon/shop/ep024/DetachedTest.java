package dev.krocon.shop.ep024;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import jakarta.persistence.EntityManager;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** Folge 024: In Spring ist der Persistence Context transaction-scoped. */
@ShopJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)   // echte Transaktionsgrenzen
class DetachedTest {

  @Autowired EntityManager em;
  @Autowired TransactionTemplate tx;

  @Test
  void entityIsDetachedAfterTransaction() {
    Order order = tx.execute(status -> {
      Order o = em.find(Order.class, 1L);
      assertThat(em.contains(o)).isTrue();     // managed innerhalb der Transaktion
      return o;
    });

    assertThat(em.contains(order)).isFalse();  // Transaktion vorbei → detached
    // Lazy-Beziehung lässt sich ohne Context nicht mehr nachladen
    assertThatThrownBy(() -> order.getCustomer().getName())
        .isInstanceOf(LazyInitializationException.class);
  }

  @Test
  void twoTransactions_twoContexts_twoObjects() {
    Order a = tx.execute(s -> em.find(Order.class, 1L));
    Order b = tx.execute(s -> em.find(Order.class, 1L));
    assertThat(a).isNotSameAs(b);              // Identität gilt nur pro Persistence Context
  }
}
