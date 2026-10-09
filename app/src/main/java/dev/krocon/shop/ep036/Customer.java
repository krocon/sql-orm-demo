package dev.krocon.shop.ep036;

import jakarta.persistence.*;
import org.hibernate.annotations.SoftDelete;

/** Kunde mit Soft Delete – zeigt die Unique-Falle (Partial Index in V351). */
@Entity(name = "Ep036Customer")
@Table(name = "ep036_customers")
@SoftDelete
public class Customer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String email;

  protected Customer() { }

  public Customer(String email) {
    this.email = email;
  }

  public Long getId() { return id; }
  public String getEmail() { return email; }
}
