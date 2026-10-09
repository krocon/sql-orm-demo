package dev.krocon.shop.ep023;

import jakarta.persistence.*;

/** Folge 023 (Problem): Cascade REMOVE auf @ManyToOne zu einem geteilten Produkt. */
@Entity
@Table(name = "ep023_order_items")
public class Ep023WrongCascadeItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)   // FALSCH
  @JoinColumn(name = "product_id")
  private Ep023Product product;

  @Column(nullable = false)
  private int quantity;

  protected Ep023WrongCascadeItem() { }

  public Ep023WrongCascadeItem(Ep023Product product, int quantity) {
    this.product = product;
    this.quantity = quantity;
  }

  public Long getId() { return id; }
}
