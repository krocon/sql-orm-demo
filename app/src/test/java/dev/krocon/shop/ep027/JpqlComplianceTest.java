package dev.krocon.shop.ep027;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Customer;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderItem;
import dev.krocon.shop.domain.OrderStatus;
import dev.krocon.shop.domain.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.hibernate.jpa.HibernatePersistenceConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Folge 027: „Window Functions und CTEs kann HQL, Standard-JPQL nicht.“
 * Mit hibernate.jpa.compliance.query=true akzeptiert Hibernate nur Standard-JPQL.
 * Eigene, strikte EntityManagerFactory nur mit den Shop-Entities – so prüft der
 * Strict-Modus nicht auch die HQL-Abfragen anderer Folgen beim Start.
 */
@ShopJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JpqlComplianceTest {

  @Autowired DataSource dataSource;

  EntityManagerFactory strictEmf;
  EntityManager em;
  OrderQueries queries;

  @BeforeAll
  void strictJpql() {
    strictEmf = new HibernatePersistenceConfiguration("ep027-strict")
        .managedClasses(Customer.class, Order.class, OrderItem.class, Product.class)
        .property("jakarta.persistence.nonJtaDataSource", dataSource)
        .property("hibernate.jpa.compliance.query", true)
        .createEntityManagerFactory();
    em = strictEmf.createEntityManager();
    queries = new OrderQueries(em);
  }

  @AfterAll
  void close() {
    em.close();
    strictEmf.close();
  }

  @Test
  void hqlWindowFunctionAndCteRejectedInStrictJpqlMode() {
    assertThatThrownBy(() -> queries.topOrderPerCustomerHql(5))
        .satisfies(e -> System.out.println("Strict JPQL: " + e))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("use of CTEs");
  }

  @Test
  void windowFunctionAloneRejectedInStrictJpqlMode() {
    assertThatThrownBy(() -> em.createQuery(
        "select o.id, rank() over (order by o.total desc) from Order o", Object[].class).getResultList())
        .satisfies(e -> System.out.println("Strict JPQL: " + e))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("non-standard function call [rank]");
  }

  @Test
  void plainJpqlStillWorks() {
    assertThat(queries.summaries(OrderStatus.PAID)).hasSize(2000);
  }
}
