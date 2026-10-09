# Folge 004 – GROUP BY & Aggregationen   (EN: GROUP BY & aggregations explained)
Video: „GROUP BY & Aggregationen in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Aggregatfunktionen verdichten viele Zeilen zu einem Wert; mit `GROUP BY` entsteht eine Zeile pro
Gruppe. Jede Spalte im SELECT ist gruppiert oder aggregiert. `WHERE` filtert Zeilen vorher,
`HAVING` Gruppen nachher. Dazu die NULL-Fallen von `COUNT` und `SUM`.

## Ausprobieren
```bash
for f in umsatz_pro_land error null; do psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/004/$f.sql; done
```

## Dateien
- `umsatz_pro_land.sql` – Abfrage aus dem Video, dazu HAVING mit Wirkung, WHERE + HAVING, die fünf Aggregate.
- `error.sql` – absichtlicher Fehler: nicht gruppierte Spalte (Code-Fenster „Problem“). Setzt `\set ON_ERROR_STOP 0`, damit `scripts/run-all.sh` weiterläuft; danach zwei korrekte Varianten.
- `null.sql` – `COUNT(*)` vs. `COUNT(spalte)`, `SUM` ohne Zeilen, `COALESCE`.

## Was du sehen solltest
- Umsatz pro Land: DE 2.142 Bestellungen / ≈ 442.000 €, AT/NL/CH/FR je ≈ 146.000–149.000 €.
  Mit den Demo-Daten liegen **alle 5 Länder über 10.000 €**; `HAVING SUM(o.total) > 200000` lässt nur DE übrig.
- `error.sql` (PostgreSQL 18):
  `ERROR:  column "customers.name" must appear in the GROUP BY clause or be used in an aggregate function`
- `null.sql`: LEFT JOIN Kunden→Bestellungen: `COUNT(*)` = 5.001, `COUNT(o.id)` = 5.000 (Anas NULL-Zeile);
  `COUNT(email)` = `COUNT(*)` = 1.000, weil email NOT NULL ist; `SUM` über 0 Zeilen = NULL, mit `COALESCE` = 0.
