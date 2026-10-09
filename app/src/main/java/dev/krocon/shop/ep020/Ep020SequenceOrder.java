package dev.krocon.shop.ep020;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Folge 020 (Hook): SEQUENCE – ID vor dem INSERT bekannt, Insert-Batching möglich. */
@Entity
@Table(name = "ep020_sequence_orders")
public class Ep020SequenceOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ep020_seq")
  @SequenceGenerator(name = "ep020_seq", sequenceName = "ep020_sequence_orders_seq", allocationSize = 50)
  private Long id;          // ID vor dem INSERT bekannt

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal total;

  protected Ep020SequenceOrder() { }      // no-arg-Konstruktor für JPA

  public Ep020SequenceOrder(Long customerId, BigDecimal total) {
    this.customerId = customerId;
    this.total = total;
  }

  public Long getId() { return id; }
  public BigDecimal getTotal() { return total; }

  // equals/hashCode null-sicher: vor persist ist id noch null
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Ep020SequenceOrder other)) return false;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return Ep020SequenceOrder.class.hashCode();   // konstant: ändert sich nicht, wenn die ID kommt
  }
}
