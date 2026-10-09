package dev.krocon.shop.ep022;

import jakarta.persistence.*;

/** Folge 022 (Hook): Owning Side – hat den Fremdschlüssel order_id. */
@Entity
@Table(name = "ep022_order_items")
public class Ep022OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)      // in OrderItem
  @JoinColumn(name = "order_id")          // Owning Side: hat den FK
  private Ep022Order order;

  @Column(nullable = false)
  private String sku;

  @Column(nullable = false)
  private int quantity;

  protected Ep022OrderItem() { }

  public Ep022OrderItem(String sku, int quantity) {
    this.sku = sku;
    this.quantity = quantity;
  }

  public Long getId() { return id; }
  public Ep022Order getOrder() { return order; }
  void setOrder(Ep022Order order) { this.order = order; }
}
