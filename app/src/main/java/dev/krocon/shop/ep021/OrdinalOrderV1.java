package dev.krocon.shop.ep021;

import jakarta.persistence.*;

/**
 * Folge 021 (Problem): @Enumerated ohne Typ = ORDINAL. Version 1 der App kennt
 * NEW(0), PAID(1), SHIPPED(2).
 */
@Entity
@Table(name = "ep021_ordinal_orders")
public class OrdinalOrderV1 {

  public enum Status { NEW, PAID, SHIPPED }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated  // Default ORDINAL: speichert 0, 1, 2 …
  @Column(nullable = false)
  private Status status;

  protected OrdinalOrderV1() { }

  public OrdinalOrderV1(Status status) { this.status = status; }

  public Long getId() { return id; }
  public Status getStatus() { return status; }
}
