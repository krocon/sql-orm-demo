package dev.krocon.shop.ep011;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Customer;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import dev.krocon.shop.ep011.jdbc.JdbcOrder;
import dev.krocon.shop.ep011.jdbc.OrderDao;
import jakarta.persistence.EntityManager;
import java.sql.SQLException;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/** Folge 011: ResultSet von Hand (Problem) vs. Entity (Lösung). */
@ShopJpaTest
@Import(OrderDao.class)
class OrderRepositoryTest {

  @Autowired EntityManager em;
  @Autowired OrderDao orderDao;

  Statistics stats;

  @BeforeEach
  void resetStats() {
    stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    stats.clear();
  }

  @Test
  void handMapping_works_butCustomerIsOnlyANumber() throws SQLException {
    List<JdbcOrder> orders = orderDao.findAll();

    assertThat(orders).hasSize(5000);
    JdbcOrder o = orders.stream().filter(x -> x.getId() == 1001L).findFirst().orElseThrow();
    assertThat(o.getCustomerId()).isPositive();   // Long, kein Customer
  }

  @Test
  void handMapping_typoInColumnName_failsOnlyAtRuntime() {
    // kompiliert problemlos, knallt erst live
    assertThatThrownBy(() -> orderDao.findAllWithTypo())
      .isInstanceOf(SQLException.class)
      .hasMessageContaining("totl")
      .satisfies(e -> System.out.println("Tippfehler: " + e));
  }

  @Test
  void entity_classToTable_fieldToColumn_referenceToForeignKey() throws SQLException {
    JdbcOrder row = orderDao.findAll().stream()
      .filter(x -> x.getId() == 1001L).findFirst().orElseThrow();
    stats.clear();

    Order order = em.find(Order.class, 1001L);           // SELECT … FROM orders
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);

    assertThat(order.getTotal()).isEqualByComparingTo(row.getTotal());   // Feld → Spalte, BigDecimal
    Customer customer = order.getCustomer();             // Referenz → customer_id
    assertThat(customer.getId()).isEqualTo(row.getCustomerId());
    assertThat(Hibernate.isInitialized(customer)).isFalse();   // LAZY: noch ein Proxy
    assertThat(customer.getEmail()).isNotBlank();        // jetzt erst SELECT customers
    assertThat(stats.getPrepareStatementCount()).isEqualTo(2);
  }

  @Test
  void unitOfWork_writesChangesOnFlush() {
    Order order = em.find(Order.class, 1001L);
    stats.clear();

    order.setStatus(OrderStatus.SHIPPED);                // kein save(), kein SQL
    assertThat(stats.getPrepareStatementCount()).isZero();

    em.flush();                                          // spätestens beim Commit
    assertThat(stats.getEntityUpdateCount()).isEqualTo(1);   // UPDATE orders SET …
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
  }
}
