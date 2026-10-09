package dev.krocon.shop.ep022;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Folge 022 (Problem): @OneToMany ohne mappedBy und ohne @JoinColumn.
 * Hibernate mappt das als Join-Tabelle ep022_uni_orders_items
 * (die Migration V220 legt sie an, sonst schlägt ddl-auto=validate fehl).
 */
@Entity(name = "Ep022UniOrder")
@Table(name = "ep022_uni_orders")
public class UnidirectionalOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String label;

  @OneToMany(cascade = CascadeType.PERSIST)  // ohne mappedBy, ohne @JoinColumn
  private List<UnidirectionalItem> items = new ArrayList<>();

  protected UnidirectionalOrder() { }

  public UnidirectionalOrder(String label) { this.label = label; }

  public Long getId() { return id; }
  public List<UnidirectionalItem> getItems() { return items; }
}
