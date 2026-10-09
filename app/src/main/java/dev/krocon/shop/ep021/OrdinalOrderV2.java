package dev.krocon.shop.ep021;

import jakarta.persistence.*;

/**
 * Folge 021 (Problem): Version 2 der App – jemand hat DRAFT vorne ins Enum eingefügt.
 * Gleiche Tabelle, gleiche Zahlen, andere Bedeutung.
 */
@Entity
@Table(name = "ep021_ordinal_orders")
public class OrdinalOrderV2 {

  public enum Status { DRAFT, NEW, PAID, SHIPPED }   // neuer Wert vorne

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated
  @Column(nullable = false)
  private Status status;

  protected OrdinalOrderV2() { }

  public Long getId() { return id; }
  public Status getStatus() { return status; }
}
