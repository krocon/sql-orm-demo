package dev.krocon.shop.ep021;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Customer;
import dev.krocon.shop.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** Folge 021: Constraints erzwingt die DB (Migration); @NotNull prüft separat in Java. */
@ShopJpaTest
class ConstraintsTest {

  @Autowired EntityManager em;
  @Autowired JdbcTemplate jdbc;

  @Test
  void beanValidationChecksInJavaBeforeSql() {
    Customer c = em.find(Customer.class, 1L);
    Ep021Order order = new Ep021Order(null, c, BigDecimal.ONE, OrderStatus.NEW, null);
    // @NotNull schlägt beim persist an – noch bevor SQL an die DB geht
    assertThatThrownBy(() -> em.persist(order))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("orderNumber");
  }

  @Test
  void databaseEnforcesConstraintsFromMigration() {
    // Index und UNIQUE kommen aus der Migration, nicht aus @Index/@Column(unique = true)
    Integer idx = jdbc.queryForObject("""
        select count(*) from pg_indexes
        where tablename = 'ep021_orders' and indexdef like '%(customer_id)%'""", Integer.class);
    assertThat(idx).isEqualTo(1);

    // NUMERIC(12,2): 11 Stellen vor dem Komma passen nicht → DB lehnt ab
    assertThatThrownBy(() -> jdbc.update("""
        insert into ep021_orders (order_number, customer_id, total, status)
        values ('X-1', 1, 12345678901.00, 'NEW')"""))
        .hasMessageContaining("numeric field overflow");
  }
}
