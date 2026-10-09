package dev.krocon.shop.ep015;

import dev.krocon.shop.domain.Product;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Folge 015 (Problem): dieselbe Tabelle orders, aber n:m „schnell mal eben“ per @ManyToMany.
 * Nur zur Demo, die echte Lösung ist dev.krocon.shop.domain.Order + OrderItem.
 */
@Entity(name = "Ep015Order")
@Table(name = "orders")
public class ManyToManyOrder {

  @Id
  private Long id;

  @ManyToMany                          // n:m „schnell mal eben“
  @JoinTable(name = "ep015_order_products",
      joinColumns = @JoinColumn(name = "order_id"),
      inverseJoinColumns = @JoinColumn(name = "product_id"))
  private List<Product> products = new ArrayList<>();
  // Join-Tabelle: nur order_id, product_id
  // Wohin mit quantity und unitPrice? Kein Platz!

  protected ManyToManyOrder() { }

  public Long getId() { return id; }
  public List<Product> getProducts() { return products; }
}
