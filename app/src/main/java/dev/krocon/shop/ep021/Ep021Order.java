package dev.krocon.shop.ep021;

import dev.krocon.shop.domain.Customer;
import dev.krocon.shop.domain.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Folge 021 (Hook): Mapping aus dem Video. Die Constraints selbst (NOT NULL, UNIQUE, Index,
 * NUMERIC(12,2)) legt die Migration V210__ep021_mapping.sql an – die Annotationen beschreiben nur.
 */
@Entity
@Table(name = "ep021_orders", indexes = @Index(columnList = "customer_id"))
public class Ep021Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull                                              // Bean Validation: prüft in Java vor dem INSERT
  @Column(name = "order_number", nullable = false, unique = true, length = 20)
  private String orderNumber;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id")
  private Customer customer;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal total;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private OrderStatus status;

  @Embedded
  private Address shippingAddress;

  protected Ep021Order() { }

  public Ep021Order(String orderNumber, Customer customer, BigDecimal total, OrderStatus status, Address shippingAddress) {
    this.orderNumber = orderNumber;
    this.customer = customer;
    this.total = total;
    this.status = status;
    this.shippingAddress = shippingAddress;
  }

  public Long getId() { return id; }
  public String getOrderNumber() { return orderNumber; }
  public BigDecimal getTotal() { return total; }
  public OrderStatus getStatus() { return status; }
  public Address getShippingAddress() { return shippingAddress; }
}
