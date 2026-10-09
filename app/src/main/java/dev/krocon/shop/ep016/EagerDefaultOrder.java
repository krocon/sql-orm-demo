package dev.krocon.shop.ep016;

import dev.krocon.shop.domain.Customer;
import jakarta.persistence.*;

/** Folge 016: dieselbe Tabelle orders, aber @ManyToOne OHNE fetch-Angabe → JPA-Default EAGER. */
@Entity(name = "Ep016EagerOrder")
@Table(name = "orders")
public class EagerDefaultOrder {

  @Id
  private Long id;

  @ManyToOne                           // Default: FetchType.EAGER
  @JoinColumn(name = "customer_id")
  private Customer customer;

  protected EagerDefaultOrder() { }

  public Long getId() { return id; }
  public Customer getCustomer() { return customer; }
}
