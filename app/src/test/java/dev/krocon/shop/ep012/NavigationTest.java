package dev.krocon.shop.ep012;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderItem;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Folge 012, Lücken 3+4: Assoziationen und Navigation – Getter-Kette vs. JOIN. */
@ShopJpaTest
class NavigationTest {

  @Autowired EntityManager em;
  Statistics stats;

  @BeforeEach
  void resetStats() {
    stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    stats.clear();
  }

  @Test
  void getterChain_vs_join() {
    // Java: Navigation per Getter → hier 2 Queries (orders, dann customers)
    Order order = em.find(Order.class, 1001L);
    String viaGetter = order.getCustomer().getEmail();
    assertThat(stats.getPrepareStatementCount()).isEqualTo(2);

    em.clear();
    stats.clear();

    // SQL: dieselbe Information per JOIN in einer Abfrage
    String viaJoin = (String) em.createNativeQuery("""
        SELECT c.email
        FROM orders o
        JOIN customers c ON c.id = o.customer_id
        WHERE o.id = 1001""").getSingleResult();
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);

    assertThat(viaJoin).isEqualTo(viaGetter);
  }

  @Test
  void manyToMany_onlyViaJoinTable() {
    // Java: Bestellung → Produkte ist eine Liste; DB: nur über order_items
    Order order = em.find(Order.class, 1001L);
    List<String> viaObjects = order.getItems().stream()
      .map(OrderItem::getProduct).map(p -> p.getSku()).sorted().toList();

    @SuppressWarnings("unchecked")
    List<String> viaSql = em.createNativeQuery("""
        SELECT p.sku
        FROM order_items oi
        JOIN products p ON p.id = oi.product_id
        WHERE oi.order_id = 1001
        ORDER BY p.sku""").getResultList();

    assertThat(viaObjects).isNotEmpty().isEqualTo(viaSql);
  }
}
