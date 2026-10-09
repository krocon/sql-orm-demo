package dev.krocon.shop.ep012;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.ep012.model.CardPayment;
import dev.krocon.shop.ep012.model.InvoicePayment;
import dev.krocon.shop.ep012.model.Payment;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Folge 012, Lücke 2: Vererbung – SINGLE_TABLE mit Diskriminator-Spalte. */
@ShopJpaTest
class InheritanceTest {

  @Autowired EntityManager em;

  @Test
  void classHierarchy_mapsToOneTableWithDiscriminator() {
    em.persist(new CardPayment(new BigDecimal("19.90"), "4242"));
    em.persist(new InvoicePayment(new BigDecimal("49.00"), 14));
    em.flush();
    em.clear();

    // Java: polymorphe Abfrage über die Oberklasse
    List<Payment> payments = em.createQuery(
        "select p from Ep012Payment p order by p.id", Payment.class).getResultList();
    assertThat(payments).hasExactlyElementsOfTypes(CardPayment.class, InvoicePayment.class);

    // DB: eine flache Tabelle, Typ steht in einer Spalte, ungenutzte Spalten sind NULL
    @SuppressWarnings("unchecked")
    List<Object[]> rows = em.createNativeQuery(
        "SELECT type, card_last4, due_days FROM ep012_payments ORDER BY id").getResultList();
    assertThat(rows.get(0)).containsExactly("CARD", "4242", null);
    assertThat(rows.get(1)).containsExactly("INVOICE", null, 14);
  }
}
