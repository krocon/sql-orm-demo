package dev.krocon.shop.ep022;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** Folge 022 (Hook): inverse Seite – @OneToMany(mappedBy) ist nur der Spiegel. */
@Entity
@Table(name = "ep022_orders")
public class Ep022Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String label;

  @OneToMany(mappedBy = "order")          // in Order: inverse Seite
  private List<Ep022OrderItem> items = new ArrayList<>();

  protected Ep022Order() { }

  public Ep022Order(String label) { this.label = label; }

  /** Hilfsmethoden halten beide Seiten synchron. */
  public void addItem(Ep022OrderItem i) { items.add(i); i.setOrder(this); }

  public void removeItem(Ep022OrderItem i) { items.remove(i); i.setOrder(null); }

  public Long getId() { return id; }
  public List<Ep022OrderItem> getItems() { return items; }
}
