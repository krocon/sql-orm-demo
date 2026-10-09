package dev.krocon.shop.ep022;

import jakarta.persistence.*;

/** Folge 022: Position ohne Rückverweis – die Tabelle hat keine order_id-Spalte. */
@Entity(name = "Ep022UniItem")
@Table(name = "ep022_uni_items")
public class UnidirectionalItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String sku;

  protected UnidirectionalItem() { }

  public UnidirectionalItem(String sku) { this.sku = sku; }

  public Long getId() { return id; }
}
