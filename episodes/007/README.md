# Folge 007 – Transaktionen & ACID   (EN: Transactions & ACID explained)
Video: „Transaktionen & ACID in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Lagerbestand verringern und Bestellposition anlegen gehören zusammen. Im Auto-Commit ist jede Anweisung
eine eigene Transaktion – fällt die zweite aus, ist die Ware weg, aber keine Bestellung da. Zwischen
`BEGIN` und `COMMIT` gilt alles oder nichts; ein verletzter CHECK rollt die ganze Transaktion zurück.

## Ausprobieren
```bash
for f in bestellung rollback autocommit; do psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/007/$f.sql; done
```
Alle Dateien arbeiten auf Kopien von `products`/`order_items` im Schema `ep007` (jede Datei legt sie
neu an), damit COMMITs die shop-Daten der anderen Folgen nicht verändern.

### Echter Absturz mit zwei Terminals
Terminal A:
```sql
\c shop
SET search_path = ep007, public;            -- nach einem Lauf von bestellung.sql vorhanden
BEGIN;
UPDATE products SET stock = stock - 1 WHERE id = 7;
-- jetzt NICHT committen
```
Terminal B:
```sql
SELECT stock FROM ep007.products WHERE id = 7;   -- alter Wert: A hat noch nicht committet (Isolation)
SELECT pg_terminate_backend(pid) FROM pg_stat_activity
WHERE state = 'idle in transaction';             -- „Absturz“ von A
SELECT stock FROM ep007.products WHERE id = 7;   -- immer noch alter Wert: A wurde zurückgerollt
```
Dasselbe ohne `BEGIN` in Terminal A (Auto-Commit): Nach dem Abbruch zeigt B den **verringerten**
Bestand – das UPDATE war schon festgeschrieben, das INSERT kam nie an.

## Dateien
- `bestellung.sql` – die Transaktion aus dem Video mit `COMMIT` (auf den ep007-Kopien).
- `rollback.sql` – CHECK-Verletzung (`stock >= 0`) mitten in der Transaktion → `ROLLBACK`, auch die gültige Änderung ist weg; dieselbe Logik als DO-Block mit `EXCEPTION`. Setzt `\set ON_ERROR_STOP 0` (Fehler sind Absicht).
- `autocommit.sql` – Auto-Commit vs. Transaktion; der „Absturz“ wird durch ein fehlschlagendes INSERT simuliert. Ebenfalls mit `\set ON_ERROR_STOP 0`.

## Was du sehen solltest
- `bestellung.sql`: Bestand Produkt 7: 165 → 164, eine neue Position (Bestellung 1001, unit_price 84,65).
  Hinweis: `order_items.unit_price` ist NOT NULL – das INSERT braucht diese Spalte (im Video fehlt sie).
- `rollback.sql`: `ERROR: new row for relation "products" violates check constraint "products_stock_check"`,
  danach `current transaction is aborted …`; nach `ROLLBACK` Bestände unverändert (7: 165, 8: 39), 0 Positionen.
- `autocommit.sql`: Auto-Commit → Bestand 164, aber 0 Positionen; in der Transaktion → Bestand 165, 0 Positionen.
