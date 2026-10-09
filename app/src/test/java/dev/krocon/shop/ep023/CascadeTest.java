package dev.krocon.shop.ep023;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Customer;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderItem;
import dev.krocon.shop.domain.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Folge 023 (Hook): domain.Order hat
 * {@code @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)}.
 */
@ShopJpaTest
class CascadeTest {

  @Autowired EntityManager em;
  @Autowired JdbcTemplate jdbc;

  @Test
  void persistOrderSavesItems() {
    Customer ana = em.find(Customer.class, 1000L);
    Order order = new Order(ana);
    order.addItem(em.find(Product.class, 1L), 2);
    order.addItem(em.find(Product.class, 2L), 1);

    em.persist(order);        // persist(order) → alle Items werden mitgespeichert
    em.flush();

    assertThat(count("select count(*) from order_items where order_id = ?", order.getId())).isEqualTo(2);
  }

  @Test
  void removeItemDeletesOrphanOnFlush() {
    Order order = em.find(Order.class, 1L);
    OrderItem item = order.getItems().get(0);
    long before = count("select count(*) from order_items where order_id = ?", 1L);

    order.removeItem(item);   // order.removeItem(item) → DELETE des Items beim Flush
    em.flush();               // SQL-Log: delete from order_items where id=?

    assertThat(count("select count(*) from order_items where order_id = ?", 1L)).isEqualTo(before - 1);
    assertThat(em.find(Product.class, item.getProduct().getId())).isNotNull();   // Produkt bleibt
  }

  @Test
  void databaseVariantOnDeleteCascade() {
    // Alternative ohne JPA: FK order_items.order_id ... ON DELETE CASCADE (V1__create_shop.sql)
    jdbc.update("delete from orders where id = 2");
    assertThat(count("select count(*) from order_items where order_id = ?", 2L)).isZero();
  }

  private long count(String sql, Object arg) {
    return jdbc.queryForObject(sql, Long.class, arg);
  }
}
