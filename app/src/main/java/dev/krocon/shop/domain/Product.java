package dev.krocon.shop.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String sku;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(nullable = false)
  private int stock;

  @Version                      // Folge 030: Optimistic Locking
  private long version;

  protected Product() { }

  public Product(String sku, String name, BigDecimal price, int stock) {
    this.sku = sku;
    this.name = name;
    this.price = price;
    this.stock = stock;
  }

  public Long getId() { return id; }
  public String getSku() { return sku; }
  public String getName() { return name; }
  public BigDecimal getPrice() { return price; }
  public void setPrice(BigDecimal price) { this.price = price; }
  public int getStock() { return stock; }
  public void setStock(int stock) { this.stock = stock; }
  public long getVersion() { return version; }
}
