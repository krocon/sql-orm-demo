# Folge 005 – Subqueries vs. CTEs   (EN: Subqueries vs. CTEs explained)
Video: „Subqueries vs. CTEs in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Verschachtelte Subqueries liest man von innen nach außen; eine CTE (`WITH`) gibt jedem Zwischenschritt
einen Namen. PostgreSQL ≥ 12 bettet einfache CTEs in die Abfrage ein – gleicher Plan wie die Subquery.
`WITH RECURSIVE` durchläuft Bäume wie den Kategorienbaum.

## Ausprobieren
```bash
for f in verschachtelt top_kunden explain kategorien; do
  psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/005/$f.sql
done
```

## Dateien
- `verschachtelt.sql` – die dreifach verschachtelte Abfrage (Code-Fenster „Problem“), dieselbe als CTE, Beweis „gleiches Ergebnis“.
- `top_kunden.sql` – Top-10 per CTE (Code-Fenster „Hook“) und als Subquery im FROM; `NOT EXISTS`-Beispiel.
- `explain.sql` – Pläne: Subquery vs. CTE (identisch, kein „CTE Scan“) vs. `AS MATERIALIZED` (mit „CTE Scan“).
- `kategorien.sql` – `WITH RECURSIVE` über `categories(parent_id)`.

## Was du sehen solltest
- Verschachtelt und CTE: je **888 Kunden**, 0 Unterschiede.
- Top-10: beide Varianten gleich, Spitze customer_id 781 mit 1.789,10 €. Achtung: Die Demo-Daten haben
  Gleichstände (z. B. 7 Kunden mit 1.704,41 €, 4 mit 1.690,40 € um Platz 10) – deshalb sortieren die
  Repo-Dateien zusätzlich nach `customer_id` und außen nochmal nach Umsatz (ohne äußeres `ORDER BY`
  ist die Reihenfolge nicht garantiert, siehe Folge 002).
- `explain.sql`: Subquery und CTE ergeben denselben Plan (`Hash Right Semi Join`), erst `MATERIALIZED`
  erzeugt `CTE bezahlt` + `CTE Scan`. Inlining gilt nur für nicht-rekursive CTEs ohne Seiteneffekte,
  die einmal referenziert werden.
- Kategorien: Küche > Tassen/Teller, Kleidung > Shirts > Bio-Shirts (3 Ebenen); 24 Produkte unter „Kleidung“.
