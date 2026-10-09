package dev.krocon.shop.ep028;

import jakarta.persistence.*;

/** Folge 028 (Problem): ID selbst vergeben, kein @GeneratedValue, kein @Version. */
@Entity
@Table(name = "ep028_products")
public class Ep028Product {

  @Id
  private Long id;

  @Column(nullable = false)
  private String name;

  protected Ep028Product() { }

  public Ep028Product(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public Long getId() { return id; }
  public String getName() { return name; }
}
