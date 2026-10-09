package dev.krocon.shop.ep027;

import java.math.BigDecimal;

/** Ergebnis der Window-Function-Abfrage. */
public record OrderRank(Long orderId, Long customerId, BigDecimal total, Long rank) {
}
