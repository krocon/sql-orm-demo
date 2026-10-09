package dev.krocon.shop.ep028;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

/** Folge 028 (Fix): Persistable sagt Spring Data selbst, ob die Entity neu ist. */
@Entity
@Table(name = "ep028_products")
public class Ep028PersistableProduct implements Persistable<Long> {

  @Id
  private Long id;

  @Column(nullable = false)
  private String name;

  @Transient
  private boolean isNew = true;

  protected Ep028PersistableProduct() { }

  public Ep028PersistableProduct(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public Long getId() { return id; }

  @Override
  public boolean isNew() { return isNew; }

  @PostLoad
  @PostPersist
  void markNotNew() { isNew = false; }   // geladen oder gespeichert → nicht mehr neu
}
