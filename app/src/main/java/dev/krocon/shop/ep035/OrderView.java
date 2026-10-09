package dev.krocon.shop.ep035;

import dev.krocon.shop.domain.Order;
import java.math.BigDecimal;
import java.util.List;

/** DTO für die API: enthält genau das, was der Client braucht – keine Lazy-Beziehungen. */
public record OrderView(Long id, String status, BigDecimal total, String customer, List<Item> items) {

  public record Item(String sku, int quantity, BigDecimal unitPrice) { }

  /** Nur innerhalb der Service-Transaktion aufrufen (alles ist per JOIN FETCH geladen). */
  static OrderView from(Order o) {
    return new OrderView(o.getId(), o.getStatus().name(), o.getTotal(), o.getCustomer().getName(),
        o.getItems().stream()
            .map(i -> new Item(i.getProduct().getSku(), i.getQuantity(), i.getUnitPrice()))
            .toList());
  }
}
