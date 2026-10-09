package dev.krocon.shop.ep011.jdbc;

import java.math.BigDecimal;

/** Order ohne ORM: ein Datenbehälter, der Kunde ist nur eine Zahl. */
public class JdbcOrder {

  private Long id;
  private BigDecimal total;
  private Long customerId;   // nur eine Zahl, kein Customer-Objekt

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public BigDecimal getTotal() { return total; }
  public void setTotal(BigDecimal total) { this.total = total; }
  public Long getCustomerId() { return customerId; }
  public void setCustomerId(Long customerId) { this.customerId = customerId; }
}
