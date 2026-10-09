package dev.krocon.shop.ep024;

import java.math.BigDecimal;

/** Eine Zeile aus der Importdatei. */
public record ProductRow(String sku, String name, BigDecimal price) {
}
