package dev.krocon.shop.ep012;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Folge 012, Lücke 1: Identität – Java vergleicht Objekte, die DB Primärschlüssel. */
@ShopJpaTest
class IdentityTest {

  @Autowired EntityManager em;
  @Autowired EntityManagerFactory emf;

  @Test
  void samePersistenceContext_sameObject() {
    Customer a = em.find(Customer.class, 42L);
    Customer b = em.find(Customer.class, 42L);

    assertThat(a == b).isTrue();   // Persistence Context garantiert: eine Zeile = ein Objekt
  }

  @Test
  void twoPersistenceContexts_twoObjectsForTheSameRow() {
    Customer a = em.find(Customer.class, 42L);
    try (EntityManager other = emf.createEntityManager()) {   // zweiter Kontext
      Customer b = other.find(Customer.class, 42L);

      assertThat(a == b).isFalse();                 // zwei Objekte …
      assertThat(a.equals(b)).isFalse();            // … und Object.equals sagt auch „verschieden“
      assertThat(a.getId()).isEqualTo(b.getId());   // … aber dieselbe Zeile (PK 42)
    }
  }
}
