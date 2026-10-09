package dev.krocon.shop.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")   // "order" ist ein SQL-Schlüsselwort
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)   // Owning Side (Folge 022)
  @JoinColumn(name = "customer_id")
  private Customer customer;

  @Enumerated(EnumType.STRING)                           // Folge 021: STRING statt ORDINAL
  @Column(nullable = false)
  private OrderStatus status = OrderStatus.NEW;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal total = BigDecimal.ZERO;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)   // Folge 023
  private List<OrderItem> items = new ArrayList<>();

  protected Order() { }

  public Order(Customer customer) {
    this.customer = customer;
  }

  /** Hilfsmethode hält beide Seiten synchron (Folge 022). */
  public void addItem(Product product, int quantity) {
    OrderItem item = new OrderItem(this, product, quantity, product.getPrice());
    items.add(item);
    total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
  }

  public void removeItem(OrderItem item) {
    items.remove(item);
    item.setOrder(null);
    total = total.subtract(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
  }

  public Long getId() { return id; }
  public Customer getCustomer() { return customer; }
  public OrderStatus getStatus() { return status; }
  public void setStatus(OrderStatus status) { this.status = status; }
  public BigDecimal getTotal() { return total; }
  public void setTotal(BigDecimal total) { this.total = total; }
  public Instant getCreatedAt() { return createdAt; }
  public List<OrderItem> getItems() { return items; }
}
