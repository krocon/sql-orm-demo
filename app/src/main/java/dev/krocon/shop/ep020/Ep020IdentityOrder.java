package dev.krocon.shop.ep020;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Folge 020 (Problem): IDENTITY – ID erst nach dem INSERT bekannt, kein Insert-Batching. */
@Entity
@Table(name = "ep020_identity_orders")
public class Ep020IdentityOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;  // erst nach dem INSERT bekannt

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal total;

  protected Ep020IdentityOrder() { }

  public Ep020IdentityOrder(Long customerId, BigDecimal total) {
    this.customerId = customerId;
    this.total = total;
  }

  public Long getId() { return id; }
}
