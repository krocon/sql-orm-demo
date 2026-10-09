# Folge 001 – Warum SQL auch heute unverzichtbar ist   (EN: Why SQL still matters)
Video: „Warum SQL auch heute unverzichtbar ist in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Statt alle Bestellungen in die App zu laden und in einer Schleife zu filtern, beschreibt eine
einzige SQL-Abfrage das gewünschte Ergebnis – der Optimizer erledigt Filtern, Summieren und Sortieren.

## Ausprobieren
```bash
docker compose up -d
psql -h 127.0.0.1 -p 5433 -U shop -d shop -f schema/shop.sql
psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/001/umsatz.sql
psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/001/vergleich.sql   # legt Schema ep001 mit 2 Mio. Zeilen an
```

## Dateien
- `umsatz.sql` – Top-10-Kunden 2026 nach Umsatz (Code-Fenster „Hook“).
- `vergleich.sql` – 2 Mio. Bestellungen in `ep001`: alle Zeilen zum Client holen (wie `findAll()`) vs. eine Aggregat-Abfrage, mit `\timing`.

## Was du sehen solltest
- `umsatz.sql`: 10 Zeilen, Spitze „Kunde 805“ und „Kunde 922“ mit je 1.704,41 €.
- `vergleich.sql` (Werte aus unserem Lauf, gerundet; schwanken je Rechner):
  - 2 Mio. Zeilen nur zum Client übertragen: **≈ 1–2,5 s** – ohne die Java-Schleife und ohne
    die Hunderte MB Heap für 2 Mio. Entities.
  - Dieselbe Frage als `WHERE … GROUP BY … ORDER BY … LIMIT 10`: **≈ 0,2–0,3 s**, 10 Zeilen übers Netz.
