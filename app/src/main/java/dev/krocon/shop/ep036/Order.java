package dev.krocon.shop.ep036;

import dev.krocon.shop.domain.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** Bestellung mit Auditing-Feldern, Soft Delete und Envers-Historie (eigene Tabelle ep036_orders). */
@Entity(name = "Ep036Order")
@Table(name = "ep036_orders")
@SoftDelete                                       // Hibernate 6.4+: Spalte "deleted" (boolean)
@Audited                                          // Envers: jede Version in ep036_orders_aud
@EntityListeners(AuditingEntityListener.class)    // Spring Data füllt die Felder unten
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status = OrderStatus.NEW;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal total = BigDecimal.ZERO;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  @CreatedBy                                      // via AuditorAware
  @Column(name = "created_by", updatable = false)
  private String createdBy;

  @LastModifiedBy
  @Column(name = "updated_by")
  private String updatedBy;

  protected Order() { }

  public Order(BigDecimal total) {
    this.total = total;
  }

  public Long getId() { return id; }
  public OrderStatus getStatus() { return status; }
  public void setStatus(OrderStatus status) { this.status = status; }
  public BigDecimal getTotal() { return total; }
  public void setTotal(BigDecimal total) { this.total = total; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public String getCreatedBy() { return createdBy; }
  public String getUpdatedBy() { return updatedBy; }
}
