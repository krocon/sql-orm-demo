# Folge 022 – @OneToMany & Co.            (EN: Relationship mapping: @OneToMany & co.)
Video: „@OneToMany & Co. in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Die Owning Side ist die Seite mit dem Fremdschlüssel: `@ManyToOne` + `@JoinColumn(name = "order_id")` im OrderItem. `@OneToMany(mappedBy = "order")` in Order ist nur der Spiegel – was du nur dort änderst, landet nicht im FK. Hilfsmethoden `addItem/removeItem` halten beide Seiten synchron; ohne `mappedBy` erwartet Hibernate eine extra Join-Tabelle.

## Ausprobieren
```bash
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep022.**'
```

## Dateien
- `app/src/main/java/dev/krocon/shop/ep022/Ep022Order.java` / `Ep022OrderItem.java` – bidirektional mit `mappedBy` und `addItem/removeItem`
- `app/src/main/java/dev/krocon/shop/ep022/UnidirectionalOrder.java` / `UnidirectionalItem.java` – `@OneToMany` ohne `mappedBy`/`@JoinColumn`
- `app/src/main/resources/db/migration/V220__ep022_relationships.sql` – Tabellen inkl. der Join-Tabelle, die Hibernate verlangt
- `app/src/test/java/dev/krocon/shop/ep022/InverseSideTest.java` – nur `items.add()` → `order_id` bleibt NULL; `addItem()` setzt ihn
- `app/src/test/java/dev/krocon/shop/ep022/UnidirectionalJoinTableTest.java` – generiertes Schema zeigt die Join-Tabelle, INSERTs landen dort
- Shop-Modell: `domain/Order.java`, `domain/OrderItem.java` (mit `addItem/removeItem`), `domain/Product.java` (keine Collection zurück)

## Was du sehen solltest
```
nur items.add(): order_id = null
create table ep022_uni_orders_items (ep022_uni_order_id bigint not null, items_id bigint not null unique);
```
- Für das unidirektionale Mapping schreibt Hibernate zusätzlich `insert into ep022_uni_orders_items (ep022_uni_order_id,items_id) values (?,?)` – pro Position ein INSERT mehr.
- `removeItem()` erzeugt `update ep022_order_items set order_id=?,…` mit NULL.
