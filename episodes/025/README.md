# Folge 025 – Dirty Checking: Updates ohne save()            (EN: Dirty checking: updates without save())
Video: „Dirty Checking: Updates ohne save() in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Änderst du eine managed Entity, vergleicht Hibernate beim Flush mit dem Lade-Snapshot und schreibt ein UPDATE – `save()` ist überflüssig. Die Kehrseite: auch versehentliche Änderungen (Rundung nur für die Anzeige) landen in der DB. `@Transactional(readOnly = true)` verzichtet auf Snapshots und Dirty Checking; `@DynamicUpdate` schreibt nur geänderte Spalten.

## Ausprobieren
```bash
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep025.**'
```
`DirtyCheckingTest` committet wirklich (Bestellung 7) und setzt sie danach zurück.

## Dateien
- `app/src/main/java/dev/krocon/shop/ep025/OrderService.java` – `ship()` ohne save, `show()` (Falle), `showReadOnly()`, `showDto()`
- `app/src/main/java/dev/krocon/shop/ep025/OrderView.java` – Anzeige-DTO, rundet selbst
- `app/src/main/java/dev/krocon/shop/ep025/OrderDynamicUpdate.java` – Tabelle `orders` mit `@DynamicUpdate`
- `app/src/test/java/dev/krocon/shop/ep025/DirtyCheckingTest.java` – UPDATE ohne save, versehentliches UPDATE, readOnly ohne UPDATE
- `app/src/test/java/dev/krocon/shop/ep025/DynamicUpdateTest.java` – UPDATE-SQL mit und ohne `@DynamicUpdate`

## Was du sehen solltest
```
vorher 392.15 → angezeigt 392 → in der DB jetzt 392.00
```
- `ship(7)`: 1 UPDATE, Status `SHIPPED` in der DB – ohne `save()`.
- `showReadOnly(7)`: gleiche Rundung, 0 UPDATEs, DB unverändert (392.15).
- SQL-Log: `update orders set created_at=?,customer_id=?,status=?,total=? where id=?` (Standard) vs. `update orders set status=? where id=?` (`@DynamicUpdate`).
