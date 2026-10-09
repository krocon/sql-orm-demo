# Folge 024 – Der Persistence Context            (EN: The persistence context)
Video: „Der Persistence Context in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Der Persistence Context ist Identity Map, First-Level-Cache und Unit of Work: pro ID genau ein Objekt, ein zweites `find()` kostet kein SQL. In Spring gibt es einen Context pro Transaktion; danach sind Entities detached. Bei Massenimporten wächst er mit jeder Zeile – daher regelmäßig `flush()` und `clear()`.

## Ausprobieren
```bash
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep024.**'
```

## Dateien
- `app/src/main/java/dev/krocon/shop/ep024/ProductImport.java` – `importAll()` (Problem) und `importInBatches()` mit `flush()/clear()` alle 50 Zeilen
- `app/src/main/java/dev/krocon/shop/ep024/ProductRow.java` – Importzeile als Record
- `app/src/test/java/dev/krocon/shop/ep024/IdentityMapTest.java` – zweimal `find` → 1 SELECT, `a == b`; `clear()` → neues Objekt
- `app/src/test/java/dev/krocon/shop/ep024/ProductImportTest.java` – Größe des Context nach 2000 Zeilen
- `app/src/test/java/dev/krocon/shop/ep024/DetachedTest.java` – nach Transaktionsende detached, `LazyInitializationException`

## Was du sehen solltest
```
importAll: 2000 Entities im Persistence Context
importInBatches: 0 Entities im Persistence Context
```
- `find(Order.class, 1L)` zweimal → 1 Statement, `a == b`; nach `em.clear()` → 2. SELECT, anderes Objekt.
- Eine JPQL-Abfrage geht immer zur DB, liefert aber das schon managed Objekt zurück.
- Nach dem Commit: `em.contains(order) == false`, `order.getCustomer().getName()` → `LazyInitializationException`.
