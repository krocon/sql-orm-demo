package dev.krocon.shop.ep025;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

/** Folge 025: Standard-UPDATE schreibt alle Spalten, @DynamicUpdate nur die geänderten. */
@ShopJpaTest
@ExtendWith(OutputCaptureExtension.class)
class DynamicUpdateTest {

  @Autowired EntityManager em;

  @Test
  void compareUpdateStatements(CapturedOutput output) {
    em.find(Order.class, 3L).setStatus(OrderStatus.NEW);   // Bestellungen 3 und 4 sind nicht NEW
    em.flush();
    em.clear();
    em.find(OrderDynamicUpdate.class, 4L).setStatus(OrderStatus.NEW);   // Bestellungen 3 und 4 sind nicht NEW
    em.flush();

    // SQL-Log (org.hibernate.SQL: debug)
    assertThat(output.getOut()).contains("update orders set created_at=?,customer_id=?,status=?,total=? where id=?");
    assertThat(output.getOut()).contains("update orders set status=? where id=?");
  }
}
