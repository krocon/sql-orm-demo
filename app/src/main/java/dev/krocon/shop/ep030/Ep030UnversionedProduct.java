package dev.krocon.shop.ep030;

import jakarta.persistence.*;

/**
 * Folge 030 – SO NICHT: dieselbe Tabelle products, aber OHNE @Version.
 * Hibernate schreibt dann blind: UPDATE products SET stock=? WHERE id=?
 */
@Entity(name = "Ep030UnversionedProduct")
@Table(name = "products")
public class Ep030UnversionedProduct {

  @Id
  private Long id;

  private int stock;

  protected Ep030UnversionedProduct() { }

  public Long getId() { return id; }
  public int getStock() { return stock; }
  public void setStock(int stock) { this.stock = stock; }
}
