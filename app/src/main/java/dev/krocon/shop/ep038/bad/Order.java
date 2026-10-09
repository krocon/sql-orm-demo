package dev.krocon.shop.ep038.bad;

import dev.krocon.shop.domain.Customer;
import dev.krocon.shop.domain.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Anti-Pattern (wie im Video): @ManyToOne mit Standard-Fetch (EAGER) und CascadeType.ALL.
 * Gleiche Tabelle orders wie die gute domain.Order – nur anders (falsch) gemappt.
 */
@Entity(name = "Ep038BadOrder")
@Table(name = "orders")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(cascade = CascadeType.ALL)  // EAGER + Cascade
  @JoinColumn(name = "customer_id")
  private Customer customer;

  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  private BigDecimal total;

  protected Order() { }

  public Long getId() { return id; }
  public Customer getCustomer() { return customer; }
  public OrderStatus getStatus() { return status; }
  public BigDecimal getTotal() { return total; }
}
