package dev.krocon.shop.ep021;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Folge 021: Value Object – Felder werden Spalten der Tabelle der besitzenden Entity. */
@Embeddable
public record Address(
    @Column(name = "ship_street", length = 100) String street,
    @Column(name = "ship_city", length = 100) String city,
    @Column(name = "ship_zip", length = 10) String zip,
    @Column(name = "ship_country", length = 2) String country) {
}
