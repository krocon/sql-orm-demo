# Folge 002 – SQL-Basics: SELECT, WHERE, ORDER BY   (EN: SQL basics: SELECT, WHERE, ORDER BY)
Video: „SQL-Basics: SELECT, WHERE, ORDER BY in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Nur benötigte Spalten holen, Zeilen mit `WHERE` filtern (NULL nur mit `IS NULL`) und immer
`ORDER BY` schreiben, wenn die Reihenfolge zählt. Die Datenbank wertet `FROM → WHERE → SELECT → ORDER BY`
aus – deshalb klappt ein Alias in `ORDER BY`, aber nicht in `WHERE`.

## Ausprobieren
```bash
for f in kunden null alias alias_fehler; do
  psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/002/$f.sql
done
```

## Dateien
- `kunden.sql` – Abfrage aus dem Video (deutsche Kunden seit 2026, neueste zuerst, `LIMIT 20`).
- `null.sql` – `= NULL` vs. `IS NULL`, `<>` mit NULL, `IS DISTINCT FROM` (kleine Tabelle in Schema `ep002`, weil `customers.email` NOT NULL ist).
- `alias.sql` – Alias in `ORDER BY` funktioniert; Lösung für `WHERE`: Ausdruck wiederholen.
- `alias_fehler.sql` – absichtlicher Fehler „Alias in WHERE“. Setzt `\set ON_ERROR_STOP 0`, damit `scripts/run-all.sh` weiterläuft; die Fehlermeldung erscheint trotzdem in der Ausgabe.

## Was du sehen solltest
- `kunden.sql`: 20 Zeilen (von 117 Treffern), neueste „Kunde 597“ vom 21.08.2026.
- `null.sql`: `WHERE email = NULL` → **0 Zeilen** (auch auf `customers`), `IS NULL` → 1 Zeile (Ben),
  `email <> 'ana@shop.de'` → nur Cem (die NULL-Zeile fällt auch hier raus), `IS DISTINCT FROM` → Ben und Cem.
- `alias_fehler.sql`: `ERROR:  column "registriert" does not exist`.
