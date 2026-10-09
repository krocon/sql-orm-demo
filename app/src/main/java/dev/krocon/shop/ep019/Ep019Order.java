package dev.krocon.shop.ep019;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;      // Standard: JPA 3.2
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.hibernate.annotations.BatchSize; // bewusst: Hibernate-Extra

/**
 * Folge 019: dieselbe Tabelle orders wie domain.Order, aber mit genau einem
 * bewusst gesetzten Hibernate-Extra. Alles andere ist reines Jakarta Persistence.
 */
@Entity(name = "Ep019Order") @Table(name = "orders")
@BatchSize(size = 20)   // lädt bis zu 20 Lazy-Proxies dieser Entity in einer Query
public class Ep019Order {

  @Id
  private Long id;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal total;

  protected Ep019Order() { }

  public Long getId() { return id; }
  public BigDecimal getTotal() { return total; }
}
