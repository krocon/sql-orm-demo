package dev.krocon.shop.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** n:m zwischen Order und Product mit Zusatzdaten → eigene Entity (Folgen 015, 022). */
@Entity
@Table(name = "order_items")
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id")
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)   // kein Cascade auf geteilte Produkte (Folge 023)
  @JoinColumn(name = "product_id")
  private Product product;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal unitPrice;   // Preis zum Bestellzeitpunkt (Folge 008)

  protected OrderItem() { }

  OrderItem(Order order, Product product, int quantity, BigDecimal unitPrice) {
    this.order = order;
    this.product = product;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
  }

  public Long getId() { return id; }
  public Order getOrder() { return order; }
  void setOrder(Order order) { this.order = order; }
  public Product getProduct() { return product; }
  public int getQuantity() { return quantity; }
  public BigDecimal getUnitPrice() { return unitPrice; }
}
