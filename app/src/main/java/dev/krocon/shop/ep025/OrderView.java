package dev.krocon.shop.ep025;

import dev.krocon.shop.domain.Order;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Anzeige-DTO: Rundung passiert hier, nicht in der Entity. */
public record OrderView(Long id, String status, BigDecimal total, BigDecimal roundedTotal) {

  public static OrderView of(Order o) {
    return new OrderView(o.getId(), o.getStatus().name(), o.getTotal(), o.getTotal().setScale(0, RoundingMode.HALF_UP));
  }
}
