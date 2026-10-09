package dev.krocon.shop.ep016;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Customer;
import dev.krocon.shop.domain.Order;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Folge 016: JPA-Defaults – ToOne ist EAGER, ToMany ist LAZY. */
@ShopJpaTest
class EagerDefaultTest {

  @Autowired EntityManager em;
  Statistics stats;

  @BeforeEach
  void resetStats() {
    stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    stats.clear();
  }

  @Test
  void manyToOne_withoutFetch_isEager() {
    EagerDefaultOrder order = em.find(EagerDefaultOrder.class, 1001L);

    assertThat(Hibernate.isInitialized(order.getCustomer())).isTrue();   // Kunde immer mit dabei
    assertThat(stats.getEntityLoadCount()).isEqualTo(2);                 // Order + Customer
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);           // per JOIN im selben SELECT
  }

  @Test
  void manyToOne_lazy_isProxyUntilAccess() {
    Order order = em.find(Order.class, 1001L);

    assertThat(Hibernate.isInitialized(order.getCustomer())).isFalse(); // Proxy
    assertThat(stats.getEntityLoadCount()).isEqualTo(1);
    order.getCustomer().getName();                                       // jetzt erst SELECT
    assertThat(stats.getPrepareStatementCount()).isEqualTo(2);
  }

  @Test
  void oneToMany_isLazyByDefault() {
    Customer customer = em.find(Customer.class, 42L);   // @OneToMany ohne fetch-Angabe
    assertThat(Hibernate.isInitialized(customer.getOrders())).isFalse();
  }
}
