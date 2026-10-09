package dev.krocon.shop.ep032;

import jakarta.persistence.*;

/** Folge 032: IDENTITY-ID → jede Zeile ein eigenes INSERT, kein Batching. */
@Entity(name = "Ep032IdentityItem")
@Table(name = "ep032_identity_items")
public class Ep032IdentityItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  protected Ep032IdentityItem() { }

  public Ep032IdentityItem(String name) {
    this.name = name;
  }

  public Long getId() { return id; }
  public String getName() { return name; }
}
