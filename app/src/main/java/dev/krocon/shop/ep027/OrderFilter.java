package dev.krocon.shop.ep027;

import dev.krocon.shop.domain.OrderStatus;
import java.math.BigDecimal;

/** Optionale Filter – null heißt „egal“. */
public record OrderFilter(OrderStatus status, BigDecimal minTotal, String email) {
}
