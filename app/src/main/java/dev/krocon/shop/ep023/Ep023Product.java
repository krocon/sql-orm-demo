package dev.krocon.shop.ep023;

import jakarta.persistence.*;

/** Folge 023: geteilte Entity – gehört keiner Bestellposition allein. */
@Entity
@Table(name = "ep023_products")
public class Ep023Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  protected Ep023Product() { }

  public Ep023Product(String name) { this.name = name; }

  public Long getId() { return id; }
}
