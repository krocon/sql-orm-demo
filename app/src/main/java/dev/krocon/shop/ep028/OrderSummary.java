package dev.krocon.shop.ep028;

import java.math.BigDecimal;

/** Record als DTO-Projection: Spring Data lädt nur diese Spalten. */
public record OrderSummary(Long id, String customerEmail, BigDecimal total) {
}
