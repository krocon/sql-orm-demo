package dev.krocon.shop.ep022;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** Folge 022: Nur die Owning Side (OrderItem.order) schreibt den Fremdschlüssel. */
@ShopJpaTest
class InverseSideTest {

  @Autowired EntityManager em;
  @Autowired JdbcTemplate jdbc;

  @Test
  void onlyInverseSideChanged_orderIdStaysNull() {
    Ep022Order order = new Ep022Order("nur items.add()");
    Ep022OrderItem item = new Ep022OrderItem("MUG-1", 2);
    order.getItems().add(item);          // nur der Spiegel geändert
    em.persist(order);
    em.persist(item);
    em.flush();

    Long orderId = jdbc.queryForObject("select order_id from ep022_order_items where id = ?", Long.class, item.getId());
    System.out.println("nur items.add(): order_id = " + orderId);
    assertThat(orderId).isNull();        // Position ohne Bestellung in der DB
  }

  @Test
  void addItemSetsBothSides() {
    Ep022Order order = new Ep022Order("addItem()");
    Ep022OrderItem item = new Ep022OrderItem("MUG-1", 2);
    order.addItem(item);                 // setzt auch item.order
    em.persist(order);
    em.persist(item);
    em.flush();

    Long orderId = jdbc.queryForObject("select order_id from ep022_order_items where id = ?", Long.class, item.getId());
    assertThat(orderId).isEqualTo(order.getId());

    order.removeItem(item);              // beide Seiten wieder lösen
    em.flush();
    assertThat(jdbc.queryForObject("select order_id from ep022_order_items where id = ?", Long.class, item.getId()))
        .isNull();
  }
}
