package dev.krocon.shop.ep026;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Folge 026: Registrierung – falsch und zweimal richtig. */
@Service("ep026RegisterService")
public class RegisterService {

  private final Ep026CustomerRepository customerRepository;
  private final MailService mailService;
  private final ApplicationEventPublisher events;

  public RegisterService(Ep026CustomerRepository customerRepository, MailService mailService,
      ApplicationEventPublisher events) {
    this.customerRepository = customerRepository;
    this.mailService = mailService;
    this.events = events;
  }

  /** Problem (Video): Mail geht raus, bevor die DB den UNIQUE-Fehler meldet. */
  @Transactional
  public void register(Ep026Customer c) {
    customerRepository.save(c);    // noch kein INSERT
    mailService.sendWelcome(c);    // Mail ist raus …
  }  // Flush beim Commit: email schon vergeben → Rollback

  /** Fix 1: saveAndFlush – Constraint-Fehler kommt sofort, vor der Mail. */
  @Transactional
  public void registerAndFlush(Ep026Customer c) {
    customerRepository.saveAndFlush(c);
    mailService.sendWelcome(c);
  }

  /** Fix 2: Mail per Event erst nach dem Commit. */
  @Transactional
  public void registerAfterCommit(Ep026Customer c) {
    customerRepository.save(c);
    events.publishEvent(new CustomerRegistered(c));
  }
}
