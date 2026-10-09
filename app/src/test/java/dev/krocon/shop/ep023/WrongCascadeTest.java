package dev.krocon.shop.ep023;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.krocon.shop.ShopJpaTest;
import jakarta.persistence.EntityManager;
import org.hibernate.TransientPropertyValueException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** Folge 023 (Problem): em.remove(item) will auch das geteilte Produkt löschen. */
@ShopJpaTest
class WrongCascadeTest {

  @Autowired EntityManager em;
  @Autowired JdbcTemplate jdbc;

  @Test
  void sharedProduct_foreignKeyError() {
    Ep023Product mug = new Ep023Product("Tasse");
    em.persist(mug);
    Ep023WrongCascadeItem a = new Ep023WrongCascadeItem(mug, 1);
    Ep023WrongCascadeItem b = new Ep023WrongCascadeItem(mug, 3);   // zweite Bestellung, gleiches Produkt
    em.persist(a);
    em.persist(b);
    em.flush();
    em.clear();     // wie im echten Leben: die andere Bestellung ist nicht geladen

    em.remove(em.find(Ep023WrongCascadeItem.class, a.getId()));   // → will auch das Produkt löschen!
    assertThatThrownBy(em::flush)
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("ep023_order_items_product_id_fkey");   // FK von Position b auf das Produkt
  }

  @Test
  void sharedProduct_otherItemLoaded_hibernateComplains() {
    Ep023Product mug = new Ep023Product("Tasse");
    em.persist(mug);
    Ep023WrongCascadeItem a = new Ep023WrongCascadeItem(mug, 1);
    Ep023WrongCascadeItem b = new Ep023WrongCascadeItem(mug, 3);
    em.persist(a);
    em.persist(b);
    em.flush();

    em.remove(a);   // b ist noch managed und zeigt auf das jetzt gelöschte Produkt
    assertThatThrownBy(em::flush)
        .hasCauseInstanceOf(TransientPropertyValueException.class);
  }

  @Test
  void productOnlyHere_silentlyGone() {
    Ep023Product mug = new Ep023Product("Tasse");
    em.persist(mug);
    Ep023WrongCascadeItem a = new Ep023WrongCascadeItem(mug, 1);
    em.persist(a);
    em.flush();

    em.remove(a);
    em.flush();     // SQL-Log: delete from ep023_order_items …; delete from ep023_products …

    Long products = jdbc.queryForObject("select count(*) from ep023_products where id = ?", Long.class, mug.getId());
    assertThat(products).isZero();   // Produkt ist weg – für alle künftigen Bestellungen
  }
}
