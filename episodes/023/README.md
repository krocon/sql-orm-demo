# Folge 023 – Cascade & Orphan Removal            (EN: Cascade & orphan removal)
Video: „Cascade & Orphan Removal in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
`cascade = ALL` plus `orphanRemoval = true` gehört auf `Order.items`: `persist(order)` speichert die Positionen mit, `removeItem()` löscht die Position beim Flush. `CascadeType.REMOVE` auf einem `@ManyToOne` zu einem geteilten Product ist dagegen gefährlich: Hibernate will das Produkt mitlöschen. Alternative auf DB-Ebene: `ON DELETE CASCADE`.

## Ausprobieren
```bash
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep023.**'
```

## Dateien
- `app/src/main/java/dev/krocon/shop/domain/Order.java` – `cascade = CascadeType.ALL, orphanRemoval = true` auf `items` (Shop-Modell)
- `app/src/main/java/dev/krocon/shop/ep023/Ep023WrongCascadeItem.java` – FALSCH: `@ManyToOne(cascade = CascadeType.REMOVE)` auf das Produkt
- `app/src/main/java/dev/krocon/shop/ep023/Ep023Product.java` – geteiltes Produkt
- `app/src/main/resources/db/migration/V230__ep023_cascade.sql` – Mini-Tabellen `ep023_*`; `ON DELETE CASCADE` steht bereits in `V1__create_shop.sql` (`order_items.order_id`)
- `app/src/test/java/dev/krocon/shop/ep023/CascadeTest.java` – Items mitspeichern, Orphan-DELETE, DB-Variante
- `app/src/test/java/dev/krocon/shop/ep023/WrongCascadeTest.java` – REMOVE auf Product: FK-Fehler oder Produkt weg

## Was du sehen solltest
- `persist(order)` → 1 INSERT in `orders` + 2 INSERTs in `order_items`.
- `order.removeItem(item)` + `flush()` → `delete from order_items where id=?`, das Produkt bleibt.
- Falsche Cascade, Produkt steckt in einer weiteren Position: `delete from ep023_products …` scheitert mit
  `violates foreign key constraint "ep023_order_items_product_id_fkey"`.
- Ist die andere Position im selben Persistence Context geladen, bricht Hibernate schon vorher mit `TransientPropertyValueException` ab.
- Steckt das Produkt nur in dieser Position: kein Fehler – das Produkt ist still gelöscht.
