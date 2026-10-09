package dev.krocon.shop.ep018;

import jakarta.persistence.*;
import java.math.BigDecimal;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Folge 018: dieselbe Tabelle products wie domain.Product, aber Second-Level-Cache-fähig.
 * Wirkt nur, wenn hibernate.cache.use_second_level_cache aktiv ist (siehe SecondLevelCacheTest).
 */
@Entity(name = "Ep018Product")
@Table(name = "products")
@Cacheable                                    // Second-Level-Cache
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CachedProduct {

  @Id
  private Long id;

  @Column(nullable = false)
  private String sku;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  protected CachedProduct() { }

  public Long getId() { return id; }
  public String getSku() { return sku; }
  public String getName() { return name; }
  public BigDecimal getPrice() { return price; }
}
