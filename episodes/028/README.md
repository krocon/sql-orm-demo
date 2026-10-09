# Folge 028 – Spring Data JPA            (EN: Spring Data JPA)
Video: „Spring Data JPA in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Ein Interface, das `JpaRepository` erweitert, liefert CRUD, Sortierung und Paging. Query-Methoden wie `findByCustomerEmailAndStatus` leitet Spring Data aus dem Namen ab; für mehr gibt es `@Query`, Projections (Record/Interface) und `Pageable`. `save()` prüft `isNew`: neu → `persist`, sonst `merge` – bei selbst vergebenen IDs kostet das ein extra SELECT.

## Ausprobieren
```bash
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep028.**'
```

## Dateien
- `app/src/main/java/dev/krocon/shop/ep028/Ep028OrderRepository.java` – Repository aus dem Video (eigener Name, weil `repo/OrderRepository` den Bean-Namen belegt) plus „zu langer“ Methodenname
- `app/src/main/java/dev/krocon/shop/ep028/OrderSummary.java` – Record-Projection `(id, customerEmail, total)`
- `app/src/main/java/dev/krocon/shop/ep028/OrderTotalView.java` – Interface-Projection
- `app/src/main/java/dev/krocon/shop/ep028/Ep028Product.java` / `Ep028PersistableProduct.java` – eigene ID ohne bzw. mit `Persistable`
- `app/src/main/java/dev/krocon/shop/ep028/Ep028ProductRepository.java`, `Ep028PersistableProductRepository.java`
- `app/src/main/resources/db/migration/V280__ep028_products.sql` – `ep028_products(id bigint PRIMARY KEY, name)`
- `app/src/test/java/dev/krocon/shop/ep028/OrderRepositoryTest.java` – abgeleitete Query, `@Query` + `PageRequest.of(0, 20)`, Projections
- `app/src/test/java/dev/krocon/shop/ep028/AssignedIdTest.java` – `save()` = persist vs. merge, Fix mit `Persistable`

## Was du sehen solltest
```
findLarge: Seite 1 von 42, 20 von 836 Bestellungen, 2 Queries
findByStatus → 1000 OrderSummary, z. B. OrderSummary[id=3, customerEmail=kunde781@shop.example, total=283.02]
save() mit eigener ID: 2 Statements (SELECT + INSERT)
```
- Paging-SQL: `… where o1_0.total>? fetch first ? rows only` plus `select count(*) from orders o1_0 where o1_0.total>?`.
- Record-Projection lädt nur 3 Spalten: `select o1_0.id,c1_0.email,o1_0.total from orders o1_0 join customers c1_0 …`.
- `save(new Ep028Product(42L, "Mug"))` → `select … from ep028_products where id=?` + INSERT, Rückgabe ist eine Kopie. Mit `Persistable`: nur INSERT, gleiches Objekt.
- `save()` einer neuen `Order` (ID null) → `persist`, 1 INSERT.
