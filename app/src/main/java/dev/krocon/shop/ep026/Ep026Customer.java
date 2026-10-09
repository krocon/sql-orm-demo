package dev.krocon.shop.ep026;

import jakarta.persistence.*;

/** Folge 026: Kunde mit SEQUENCE-ID – persist/save schickt noch kein INSERT. */
@Entity
@Table(name = "ep026_customers")
public class Ep026Customer {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ep026_seq")
  @SequenceGenerator(name = "ep026_seq", sequenceName = "ep026_customers_seq", allocationSize = 50)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String name;

  protected Ep026Customer() { }

  public Ep026Customer(String email, String name) {
    this.email = email;
    this.name = name;
  }

  public Long getId() { return id; }
  public String getEmail() { return email; }
}
