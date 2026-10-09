package dev.krocon.shop.ep031;

import jakarta.persistence.*;

/**
 * Folge 031: Mapping passend zu V300/V301. ddl-auto: validate prüft beim Start,
 * dass Tabelle und Spalten existieren – das Schema selbst kommt nur aus Flyway.
 */
@Entity(name = "Ep031CustomerContact")
@Table(name = "ep031_customer_contacts")
public class Ep031CustomerContact {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  private String phone;          // alt – fällt beim Contract-Schritt weg

  @Column(name = "phone_e164")
  private String phoneE164;      // neu (Expand)

  protected Ep031CustomerContact() { }

  public Long getId() { return id; }
  public Long getCustomerId() { return customerId; }
  public String getPhone() { return phone; }
  public String getPhoneE164() { return phoneE164; }
}
