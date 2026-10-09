package dev.krocon.shop.ep038;

import dev.krocon.shop.domain.Order;
import java.math.BigDecimal;

/** API-Modell: ein Record, entkoppelt vom Schema, ohne Lazy-Beziehungen. */
public record OrderDto(Long id, String status, BigDecimal total, Long customerId) {

  public static OrderDto from(Order o) {
    // getCustomer().getId() liest nur die ID aus dem Proxy – keine Query
    return new OrderDto(o.getId(), o.getStatus().name(), o.getTotal(), o.getCustomer().getId());
  }
}
