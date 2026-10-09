package dev.krocon.shop.ep027;

import java.math.BigDecimal;

/** DTO-Projektion: nur die Spalten, die gebraucht werden. */
public record OrderSummary(Long id, String email, BigDecimal total) {
}
