package dev.krocon.shop.ep020;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/** Folge 020: GenerationType.UUID (seit JPA 3.1) – ID entsteht in Java, kein DB-Roundtrip. */
@Entity
@Table(name = "ep020_uuid_orders")
public class Ep020UuidOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal total;

  protected Ep020UuidOrder() { }

  public Ep020UuidOrder(Long customerId, BigDecimal total) {
    this.customerId = customerId;
    this.total = total;
  }

  public UUID getId() { return id; }
}
