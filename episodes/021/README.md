# Folge 021 – Entity Mapping: @Column, @Table, Constraints            (EN: Entity mapping: @Column, @Table, constraints)
Video: „Entity Mapping: @Column, @Table, Constraints in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
`@Table`/`@Column` steuern Namen, `nullable`, `length`, `precision/scale`. Enums gehören als `EnumType.STRING` gemappt – der Default `ORDINAL` speichert nur die Position, ein neuer Wert vorne im Enum verdreht alte Daten. `@Embeddable` bildet Wertgruppen (Adresse) auf Spalten derselben Tabelle ab; echte Constraints legt die Migration an, `@NotNull` prüft separat in Java.

## Ausprobieren
```bash
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep021.**'
```

## Dateien
- `app/src/main/java/dev/krocon/shop/ep021/Ep021Order.java` – Mapping aus dem Video (`@Index`, `precision = 12, scale = 2`, `STRING`, `@Embedded`)
- `app/src/main/java/dev/krocon/shop/ep021/Address.java` – `@Embeddable` (Record) mit street, city, zip, country
- `app/src/main/java/dev/krocon/shop/ep021/OrdinalOrderV1.java` / `OrdinalOrderV2.java` – dieselbe Tabelle, Enum ohne bzw. mit `DRAFT` vorne
- `app/src/main/resources/db/migration/V210__ep021_mapping.sql` – NOT NULL, UNIQUE, CHECK, Index, NUMERIC(12,2)
- `app/src/test/java/dev/krocon/shop/ep021/OrdinalPitfallTest.java` – Enum umsortiert → falscher Status; STRING speichert den Namen
- `app/src/test/java/dev/krocon/shop/ep021/ConstraintsTest.java` – `@NotNull` greift in Java, Index/Precision kommen aus der DB

## Was du sehen solltest
```
Gespeichert als PAID, gelesen als NEW
Zeile in ep021_orders: {status=PAID, total=1234567890.99, ship_city=Hamburg, ship_zip=20457}
```
- In `ep021_ordinal_orders.status` steht nur `1`; Version 2 des Enums liest daraus `NEW`.
- `@NotNull` wirft `jakarta.validation.ConstraintViolationException` schon beim `persist()`, ohne SQL.
- Ein Betrag mit 11 Vorkommastellen scheitert in der DB: `numeric field overflow`.
