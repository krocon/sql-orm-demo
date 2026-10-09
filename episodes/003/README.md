# Folge 003 – JOINs: INNER, LEFT und RIGHT   (EN: JOINs explained: INNER, LEFT, RIGHT)
Video: „JOINs: INNER, LEFT und RIGHT in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
INNER JOIN liefert nur Paare, LEFT JOIN behält alle Zeilen links (fehlt rechts etwas: NULL),
RIGHT JOIN ist das Spiegelbild. Vergisst man die Join-Bedingung, entsteht ein Kreuzprodukt; eine
WHERE-Bedingung auf die rechte Tabelle macht aus einem LEFT JOIN heimlich einen INNER JOIN.

## Ausprobieren
```bash
for f in joins cross trap; do psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/003/$f.sql; done
```

## Dateien
- `joins.sql` – INNER, LEFT (Code-Fenster „Hook“), „Wer hat nie bestellt?“, RIGHT JOIN.
- `cross.sql` – vergessene Join-Bedingung, `COUNT(*)` des Kreuzprodukts (Code-Fenster „Problem“).
- `trap.sql` – `WHERE o.status = 'PAID'` vs. `AND o.status = 'PAID'` in `ON`, plus Plan.

## Was du sehen solltest
- INNER JOIN: 5.000 Zeilen; LEFT JOIN: 5.001 Zeilen – erste Zeile „Ana Beispiel“ mit `order_id = NULL`.
- Nie bestellt: genau 1 Kundin (Ana, ana@shop.de) – mit LEFT und RIGHT JOIN gleich.
- Kreuzprodukt: 1.000 Kunden × 5.000 Bestellungen = **5.000.000 Zeilen** (mit Bedingung: 5.000).
- Filterfalle: in `WHERE` → 999 Kunden / 2.000 Zeilen, in `ON` → 1.000 Kunden / 2.001 Zeilen.
  `EXPLAIN` zeigt im ersten Fall einen normalen `Hash Join` statt eines Left Joins.
