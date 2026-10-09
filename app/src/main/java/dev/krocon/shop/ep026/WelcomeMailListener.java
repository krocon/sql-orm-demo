package dev.krocon.shop.ep026;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Mail erst NACH erfolgreichem Commit – bei Rollback wird nichts verschickt. */
@Component("ep026WelcomeMailListener")
public class WelcomeMailListener {

  private final MailService mailService;

  public WelcomeMailListener(MailService mailService) {
    this.mailService = mailService;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(CustomerRegistered event) {
    mailService.sendWelcome(event.customer());
  }
}
