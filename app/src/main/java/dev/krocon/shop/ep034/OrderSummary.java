package dev.krocon.shop.ep034;

import java.math.BigDecimal;

/** Projektion für die Bestellübersicht: nur, was die Seite anzeigt. */
public record OrderSummary(Long id, BigDecimal total, String customerName) { }
