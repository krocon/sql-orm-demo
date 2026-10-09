package dev.krocon.shop.ep014;

import dev.krocon.shop.domain.Customer;
import dev.krocon.shop.repo.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Folge 014: Ändern nur, solange die Entity managed ist. */
@Service("ep014CustomerService")
public class CustomerService {

  private final CustomerRepository customers;

  public CustomerService(CustomerRepository customers) {
    this.customers = customers;
  }

  /** Problem-Hälfte: nach dem return ist die Transaktion vorbei → Entity detached. */
  @Transactional(readOnly = true)
  public Customer find(Long id) {
    return customers.findById(id).orElseThrow();
  }

  /** Lösung: Laden und Ändern in derselben Transaktion – Dirty Checking schreibt. */
  @Transactional
  public void changeEmail(Long id, String email) {
    Customer c = customers.findById(id).orElseThrow();   // MANAGED
    c.setEmail(email);                                   // UPDATE beim Commit
  }
}
