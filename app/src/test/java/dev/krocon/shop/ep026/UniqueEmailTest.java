package dev.krocon.shop.ep026;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Customer;
import dev.krocon.shop.repo.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** Folge 026: save() vs. saveAndFlush() bei vergebener E-Mail – mit echtem Commit. */
@ShopJpaTest
@Import({RegisterService.class, MailService.class, WelcomeMailListener.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class UniqueEmailTest {

  static final String TAKEN = "ana@shop.de";

  @Autowired RegisterService registerService;
  @Autowired MailService mailService;
  @Autowired Ep026CustomerRepository ep026Customers;
  @Autowired CustomerRepository customerRepository;
  @Autowired TransactionTemplate tx;
  @Autowired JdbcTemplate jdbc;

  @BeforeEach
  void setUp() {
    jdbc.update("delete from ep026_customers");
    mailService.sent().clear();
    ep026Customers.saveAndFlush(new Ep026Customer(TAKEN, "Ana"));   // E-Mail schon vergeben
  }

  @AfterEach
  void cleanUp() {
    jdbc.update("delete from ep026_customers");
  }

  @Test
  void save_failsOnlyAtCommit_mailAlreadySent() {
    assertThatThrownBy(() -> registerService.register(new Ep026Customer(TAKEN, "Ana 2")))
        .isInstanceOf(DataIntegrityViolationException.class);    // erst beim Commit
    assertThat(mailService.sent()).containsExactly(TAKEN);       // Mail ist trotzdem raus!
  }

  @Test
  void saveAndFlush_failsImmediately_noMail() {
    assertThatThrownBy(() -> registerService.registerAndFlush(new Ep026Customer(TAKEN, "Ana 2")))
        .isInstanceOf(DataIntegrityViolationException.class);
    assertThat(mailService.sent()).isEmpty();
  }

  @Test
  void afterCommitEvent_noMailOnRollback_mailOnSuccess() {
    assertThatThrownBy(() -> registerService.registerAfterCommit(new Ep026Customer(TAKEN, "Ana 2")))
        .isInstanceOf(DataIntegrityViolationException.class);
    assertThat(mailService.sent()).isEmpty();

    registerService.registerAfterCommit(new Ep026Customer("neu@shop.de", "Neu"));
    assertThat(mailService.sent()).containsExactly("neu@shop.de");
  }

  @Test
  void identityCustomer_saveFailsImmediately() {
    // domain.Customer nutzt IDENTITY: save() muss sofort INSERTen → Fehler schon hier
    tx.executeWithoutResult(s -> {
      assertThatThrownBy(() -> customerRepository.save(new Customer(TAKEN, "Ana 2", "DE")))
          .isInstanceOf(DataIntegrityViolationException.class);
      s.setRollbackOnly();
    });
  }
}
