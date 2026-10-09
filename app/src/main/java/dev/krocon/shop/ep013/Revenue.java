package dev.krocon.shop.ep013;

import java.math.BigDecimal;

/** Eine Zeile des Umsatz-Reports; country = null ist die ROLLUP-Gesamtsumme. */
public record Revenue(String country, BigDecimal total) { }
