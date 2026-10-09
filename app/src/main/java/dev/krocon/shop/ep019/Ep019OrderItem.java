package dev.krocon.shop.ep019;

import jakarta.persistence.*;

/** Position, die lazy auf Ep019Order zeigt – damit @BatchSize sichtbar wird. */
@Entity(name = "Ep019OrderItem")
@Table(name = "order_items")
public class Ep019OrderItem {

  @Id
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id")
  private Ep019Order order;

  protected Ep019OrderItem() { }

  public Long getId() { return id; }
  public Ep019Order getOrder() { return order; }
}
