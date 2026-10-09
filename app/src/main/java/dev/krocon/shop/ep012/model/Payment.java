package dev.krocon.shop.ep012.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Folge 012: Klassen erben, Tabellen nicht → Mapping-Strategie nötig. */
@Entity(name = "Ep012Payment")
@Table(name = "ep012_payments")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  protected Payment() { }

  protected Payment(BigDecimal amount) {
    this.amount = amount;
  }

  public Long getId() { return id; }
  public BigDecimal getAmount() { return amount; }
}
