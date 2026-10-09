package dev.krocon.shop.ep032;

import jakarta.persistence.*;

/** Folge 032: SEQUENCE-ID → Hibernate kennt die ID vor dem INSERT und kann batchen. */
@Entity(name = "Ep032BatchItem")
@Table(name = "ep032_batch_items")
public class Ep032BatchItem {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ep032_seq")
  @SequenceGenerator(name = "ep032_seq", sequenceName = "ep032_batch_item_seq", allocationSize = 50)
  private Long id;

  @Column(nullable = false)
  private String name;

  protected Ep032BatchItem() { }

  public Ep032BatchItem(String name) {
    this.name = name;
  }

  public Long getId() { return id; }
  public String getName() { return name; }
}
