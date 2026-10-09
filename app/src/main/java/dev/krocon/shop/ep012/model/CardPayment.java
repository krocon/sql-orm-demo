package dev.krocon.shop.ep012.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity(name = "Ep012CardPayment")
@DiscriminatorValue("CARD")
public class CardPayment extends Payment {

  @Column(name = "card_last4")
  private String cardLast4;

  protected CardPayment() { }

  public CardPayment(BigDecimal amount, String cardLast4) {
    super(amount);
    this.cardLast4 = cardLast4;
  }

  public String getCardLast4() { return cardLast4; }
}
