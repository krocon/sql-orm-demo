# Folge 030 – Optimistic vs. Pessimistic Locking            (EN: Optimistic vs. Pessimistic Locking)
Video: „Optimistic vs. Pessimistic Locking in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Zwei Admins laden Produkt 7 mit Lagerbestand 10. A zieht 3 ab, B bucht 5 dazu – B speichert zuletzt 15, die Änderung von A ist weg (Lost Update). Abhilfe: `@Version` (Konflikt → Exception → 409/Retry), `PESSIMISTIC_WRITE` (Zeilensperre, andere warten) oder ein atomares `UPDATE … SET stock = stock - 1 WHERE stock > 0`.

## Ausprobieren
```bash
cd app
./mvnw test -Dtest='dev.krocon.shop.ep030.**'
```
Die Tests laufen mit echten Commits in zwei Threads (`TransactionTemplate`) und setzen Produkt 7 danach zurück.

## Dateien
- `app/src/main/java/dev/krocon/shop/ep030/Ep030UnversionedProduct.java` – Mapping auf `products` OHNE `@Version` (für das Problem)
- `app/src/main/java/dev/krocon/shop/ep030/Ep030ProductRepository.java` – `@Lock(PESSIMISTIC_WRITE) findForUpdate`, `@Modifying decrementStock`
- `app/src/main/java/dev/krocon/shop/ep030/StockService.java` – Retry bei Konflikt, gesperrtes Ändern, `reserveOne` (false = ausverkauft)
- `app/src/main/java/dev/krocon/shop/domain/Product.java` – hat bereits `@Version` (gemeinsame Entity)
- `app/src/test/java/dev/krocon/shop/ep030/LostUpdateTest.java` – ohne Version: 15 statt 12
- `app/src/test/java/dev/krocon/shop/ep030/OptimisticLockTest.java` – mit `@Version`: `ObjectOptimisticLockingFailureException`, dann Retry → 12
- `app/src/test/java/dev/krocon/shop/ep030/PessimisticLockTest.java` – B wartet auf A, Ergebnis 12
- `app/src/test/java/dev/krocon/shop/ep030/PessimisticLockSqlTest.java` – welches SQL erzeugt welcher Lock-Modus
- `app/src/test/java/dev/krocon/shop/ep030/AtomicUpdateTest.java` – Bestand 1: erst true, dann false, nie negativ
- `app/src/test/java/dev/krocon/shop/ep030/StockFixture.java` – setzt Bestand und stellt ihn wieder her

## Was du sehen solltest
- Lost Update: zweimal `update products set stock=? where id=?`, Endstand **15** (richtig wäre 12).
- Optimistic: `update products set name=?,price=?,sku=?,stock=?,version=? where id=? and version=?` trifft bei B 0 Zeilen → `ObjectOptimisticLockingFailureException`; Bestand bleibt 7, Retry ergibt 12.
- Pessimistic (Hibernate 7.4 + PostgreSQL): `select … from products p1_0 where p1_0.id=? for no key update of p1_0`; `PESSIMISTIC_READ` → `for share of p1_0`. B wartet ca. 0,5 s, Endstand 12.
- Atomar: `update products p1_0 set stock=(p1_0.stock-1) where p1_0.id=? and p1_0.stock>0` – kein SELECT davor; Rückgabe 1, dann 0.
- Achtung: Ein JPQL-Bulk-Update erhöht `@Version` nicht (dafür gibt es `update versioned Product …`).
