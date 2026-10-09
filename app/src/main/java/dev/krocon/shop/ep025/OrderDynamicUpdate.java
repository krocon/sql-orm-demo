package dev.krocon.shop.ep025;

import dev.krocon.shop.domain.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.DynamicUpdate;

/** Folge 025: gleiche Tabelle wie domain.Order, aber @DynamicUpdate (Hibernate-Extra). */
@Entity(name = "Ep025DynamicOrder")
@Table(name = "orders")
@DynamicUpdate   // UPDATE nur mit geänderten Spalten
public class OrderDynamicUpdate {

  @Id
  private Long id;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal total;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected OrderDynamicUpdate() { }

  public Long getId() { return id; }
  public void setStatus(OrderStatus status) { this.status = status; }
}
