package dev.krocon.shop.ep012.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity(name = "Ep012InvoicePayment")
@DiscriminatorValue("INVOICE")
public class InvoicePayment extends Payment {

  @Column(name = "due_days")
  private Integer dueDays;

  protected InvoicePayment() { }

  public InvoicePayment(BigDecimal amount, int dueDays) {
    super(amount);
    this.dueDays = dueDays;
  }

  public Integer getDueDays() { return dueDays; }
}
