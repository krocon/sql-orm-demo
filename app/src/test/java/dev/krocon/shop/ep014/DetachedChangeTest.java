package dev.krocon.shop.ep014;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Customer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Folge 014: Änderung an einer detached Entity geht still verloren (open-in-view: false). */
@ShopJpaTest
@Import(CustomerService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)   // echte Commits, kein Test-Rollback
class DetachedChangeTest {

  @Autowired CustomerService customerService;
  String originalEmail;

  @BeforeEach
  void remember() {
    originalEmail = customerService.find(42L).getEmail();
  }

  @AfterEach
  void restore() {
    customerService.changeEmail(42L, originalEmail);
  }

  @Test
  void problem_changeAfterTransaction_isLost() {
    Customer c = customerService.find(42L);   // Transaktion vorbei
    c.setEmail("neu@shop.de");                // DETACHED: keiner schaut zu
    // kein merge, kein save → Änderung weg
    assertThat(customerService.find(42L).getEmail()).isEqualTo(originalEmail);
  }

  @Test
  void solution_changeInsideTransactionalMethod() {
    customerService.changeEmail(42L, "neu@shop.de");
    assertThat(customerService.find(42L).getEmail()).isEqualTo("neu@shop.de");
  }
}
