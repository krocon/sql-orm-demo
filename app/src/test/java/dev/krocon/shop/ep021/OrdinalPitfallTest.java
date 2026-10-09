package dev.krocon.shop.ep021;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Customer;
import dev.krocon.shop.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** Folge 021: ORDINAL speichert die Position – Enum umsortiert → falscher Status. */
@ShopJpaTest
class OrdinalPitfallTest {

  @Autowired EntityManager em;
  @Autowired JdbcTemplate jdbc;

  @Test
  void ordinalBreaksWhenEnumIsReordered() {
    // App-Version 1 speichert eine bezahlte Bestellung
    OrdinalOrderV1 v1 = new OrdinalOrderV1(OrdinalOrderV1.Status.PAID);
    em.persist(v1);
    em.flush();
    em.clear();

    Integer raw = jdbc.queryForObject("select status from ep021_ordinal_orders where id = ?", Integer.class, v1.getId());
    assertThat(raw).isEqualTo(1);                                   // gespeichert: nur die Zahl 1

    // App-Version 2 (DRAFT vorne eingefügt) liest dieselbe Zeile
    OrdinalOrderV2 v2 = em.find(OrdinalOrderV2.class, v1.getId());
    System.out.println("Gespeichert als PAID, gelesen als " + v2.getStatus());
    assertThat(v2.getStatus()).isEqualTo(OrdinalOrderV2.Status.NEW);   // falsch!
  }

  @Test
  void stringStoresTheName() {
    Customer ana = em.createQuery("select c from Customer c where c.email = 'ana@shop.de'", Customer.class)
        .getSingleResult();
    Ep021Order order = new Ep021Order("A-1001", ana, new java.math.BigDecimal("1234567890.99"), OrderStatus.PAID,
        new Address("Hafenstraße 1", "Hamburg", "20457", "DE"));
    em.persist(order);
    em.flush();

    var row = jdbc.queryForMap("select status, total, ship_city, ship_zip from ep021_orders where id = ?", order.getId());
    System.out.println("Zeile in ep021_orders: " + row);
    assertThat(row.get("status")).isEqualTo("PAID");              // Name statt Position
    assertThat(row.get("ship_city")).isEqualTo("Hamburg");        // @Embedded → Spalte derselben Tabelle
    assertThat(row.get("total")).hasToString("1234567890.99");    // NUMERIC(12,2)
  }
}
