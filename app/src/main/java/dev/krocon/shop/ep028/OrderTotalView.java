package dev.krocon.shop.ep028;

import java.math.BigDecimal;

/** Interface-Projection: Spring Data erzeugt einen Proxy mit nur diesen Gettern. */
public interface OrderTotalView {
  Long getId();
  BigDecimal getTotal();
}
