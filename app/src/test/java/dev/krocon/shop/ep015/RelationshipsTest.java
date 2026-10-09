package dev.krocon.shop.ep015;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Customer;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderItem;
import dev.krocon.shop.domain.Product;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Folge 015: 1:n/n:1, n:m mit und ohne Zusatzdaten, Owning Side. */
@ShopJpaTest
class RelationshipsTest {

  @Autowired EntityManager em;

  @Test
  void manyToMany_joinTableHasOnlyTwoIds() {
    ManyToManyOrder order = em.find(ManyToManyOrder.class, 1001L);
    order.getProducts().add(em.find(Product.class, 1L));
    em.flush();   // INSERT INTO ep015_order_products (order_id, product_id) …

    @SuppressWarnings("unchecked")
    List<String> columns = em.createNativeQuery("""
        SELECT column_name FROM information_schema.columns
        WHERE table_name = 'ep015_order_products' ORDER BY ordinal_position""")
      .getResultList();
    assertThat(columns).containsExactly("order_id", "product_id");   // kein Platz für quantity
  }

  @Test
  void orderItem_isTheManyToManyWithData() {
    Order order = em.find(Order.class, 1001L);

    assertThat(order.getItems()).isNotEmpty().allSatisfy((OrderItem item) -> {
      assertThat(item.getProduct()).isNotNull();          // n:1 zu Product
      assertThat(item.getQuantity()).isPositive();        // Zusatzdaten der Beziehung
      assertThat(item.getUnitPrice()).isNotNull();
    });
  }

  @Test
  void foreignKeyLivesOnTheManySide() {
    // 1:n Customer → Orders: die Spalte customer_id steht in orders, nicht in customers
    @SuppressWarnings("unchecked")
    List<String> tables = em.createNativeQuery("""
        SELECT table_name FROM information_schema.columns
        WHERE column_name = 'customer_id' AND table_schema = current_schema()""")
      .getResultList();
    assertThat(tables).contains("orders").doesNotContain("customers");
  }

  @Test
  void onlyTheOwningSideWritesTheForeignKey() {
    Order order = em.find(Order.class, 1001L);
    Customer oldCustomer = order.getCustomer();
    Customer newCustomer = em.find(Customer.class, 1000L);   // ana@shop.de

    // nur die inverse Seite (mappedBy) ändern → Hibernate ignoriert das,
    // denn den FK customer_id verwaltet die Owning Side Order.customer (@ManyToOne)
    oldCustomer.getOrders().remove(order);
    newCustomer.getOrders().add(order);
    em.flush();
    em.clear();
    assertThat(em.find(Order.class, 1001L).getCustomer().getId()).isEqualTo(oldCustomer.getId());
  }
}
