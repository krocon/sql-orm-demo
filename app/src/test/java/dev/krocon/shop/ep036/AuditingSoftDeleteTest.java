package dev.krocon.shop.ep036;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

@ShopJpaTest
@Import(AuditConfig.class)
class AuditingSoftDeleteTest {
  @Autowired OrderRepository orders;
  @Autowired EntityManager em;
  @Autowired JdbcClient jdbc;

  @AfterEach
  void logout() { CurrentUser.clear(); }

  @Test
  void auditingFields() throws Exception {
    CurrentUser.set("anna");
    Order order = orders.saveAndFlush(new Order(new BigDecimal("19.90")));
    assertThat(order.getCreatedAt()).isNotNull();
    assertThat(order.getCreatedBy()).isEqualTo("anna");
    assertThat(order.getUpdatedBy()).isEqualTo("anna");

    Thread.sleep(5);
    CurrentUser.set("ben");
    order.setStatus(OrderStatus.PAID);
    orders.saveAndFlush(order);
    assertThat(order.getCreatedBy()).isEqualTo("anna");          // bleibt
    assertThat(order.getUpdatedBy()).isEqualTo("ben");           // wer hat geändert?
    assertThat(order.getUpdatedAt()).isAfter(order.getCreatedAt());
  }

  @Test
  void softDelete_filteredEverywhere() {
    Order keep = orders.save(new Order(new BigDecimal("10.00")));
    Order gone = orders.save(new Order(new BigDecimal("99.00")));
    orders.flush();

    orders.delete(gone);       // SQL: update ep036_orders set deleted=true where id=?
    orders.flush();
    em.clear();

    // JPA-Seite: Hibernate ergänzt "where deleted = false" in jeder Query
    assertThat(orders.findById(gone.getId())).isEmpty();
    assertThat(orders.findAll()).extracting(Order::getId).contains(keep.getId()).doesNotContain(gone.getId());
    assertThat(orders.findAllJpql()).extracting(Order::getId).doesNotContain(gone.getId());

    // Die Zeile existiert noch – und natives SQL (z. B. ein Report) muss selbst filtern!
    Boolean deleted = jdbc.sql("select deleted from ep036_orders where id = ?")
        .param(gone.getId()).query(Boolean.class).single();
    assertThat(deleted).isTrue();
    long nativeCount = jdbc.sql("select count(*) from ep036_orders where id in (?, ?)")
        .params(keep.getId(), gone.getId()).query(Long.class).single();
    assertThat(nativeCount).isEqualTo(2);
  }

  @Test
  void softDelete_uniqueTrap_partialIndex() {
    Customer old = new Customer("lisa@shop.de");
    em.persist(old);
    em.flush();
    em.remove(old);            // weich gelöscht, Zeile bleibt
    em.flush();

    // Dank Partial Unique Index (WHERE NOT deleted) darf sich Lisa neu registrieren
    em.persist(new Customer("lisa@shop.de"));
    em.flush();
    assertThat(jdbc.sql("select count(*) from ep036_customers where email = 'lisa@shop.de'")
        .query(Long.class).single()).isEqualTo(2);

    // Zwei aktive Kunden mit derselben E-Mail bleiben verboten
    // (IDENTITY: das INSERT läuft schon bei persist)
    assertThatThrownBy(() -> em.persist(new Customer("lisa@shop.de")))
        .isInstanceOf(PersistenceException.class)
        .hasStackTraceContaining("ep036_customers_email_active_uq");
  }
}
