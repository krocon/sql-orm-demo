package dev.krocon.shop.ep037;

import jakarta.persistence.*;

/** Nach der Umbenennung: Feld name → fullName (Spalte full_name, siehe V371). */
@Entity(name = "Ep037Customer")
@Table(name = "ep037_customers")
public class Customer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String email;

  // vorher: name. Bewusst ohne nullable = false: dann legt ddl-auto=update die Spalte an
  // (mit NOT NULL scheitert das ALTER an den Bestandsdaten – Hibernate loggt nur eine WARNung).
  @Column(name = "full_name")
  private String fullName;

  protected Customer() { }

  public Customer(String email, String fullName) {
    this.email = email;
    this.fullName = fullName;
  }

  public Long getId() { return id; }
  public String getEmail() { return email; }
  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
}
