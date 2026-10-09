package dev.krocon.shop.ep040;

import dev.krocon.shop.domain.OrderStatus;
import java.math.BigDecimal;

/** Ergebnis des Umsatzreports – eine Zeile pro Status. */
public record StatusRevenue(OrderStatus status, BigDecimal revenue) { }
