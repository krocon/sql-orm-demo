# Folge 006 – Indexes: Warum Datenbanken langsam werden   (EN: Indexes: why databases get slow)
Video: „Indexes: Warum Datenbanken langsam werden in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Ohne passenden Index liest PostgreSQL jede Zeile (Seq Scan). Ein B-Tree-Index auf
`(customer_id, created_at)` findet die 12 Bestellungen eines Kunden direkt – hilft aber nur „von links“.
Indexes kosten Platz und Schreibzeit, und Fremdschlüssel bekommen keinen automatischen Index.

## Ausprobieren
```bash
psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/006/01_seed.sql    # Schema ep006, 1 Mio. Bestellungen (~5–10 s)
psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/006/02_index.sql   # EXPLAIN ANALYZE vorher/nachher
psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/006/03_order.sql   # Spaltenreihenfolge
```
Die Nummern im Dateinamen sorgen dafür, dass `scripts/run-all.sh` (alphabetisch) in der richtigen Reihenfolge läuft.

## Dateien
- `01_seed.sql` – `ep006.orders` mit 1.000.000 Zeilen (83.333 Kunden à ~12 Bestellungen) per `generate_series`, ohne Index auf `customer_id`, danach `ANALYZE`.
- `02_index.sql` – Abfrage für Kunde 42 vor und nach `CREATE INDEX idx_orders_customer_created`, Indexgröße.
- `03_order.sql` – Index hilft bei `customer_id` (+ `created_at`), nicht bei `created_at` allein; PG-18-Skip-Scan-Gegenprobe; FK ohne Index.

## Was du sehen solltest
Werte aus unseren Läufen (PostgreSQL 18.4, Daten im Cache), gerundet – sie schwanken je Rechner:
- Vorher: `Parallel Seq Scan on orders`, „Rows Removed by Filter“ ≈ 333.000 pro Worker (3 Prozesse,
  zusammen 1 Mio. Zeilen geprüft), **12 Treffer**, Execution Time **≈ 50–120 ms**.
- Nachher: `Bitmap Index Scan on idx_orders_customer_created` → `Bitmap Heap Scan`, 12 Zeilen,
  Execution Time **≈ 0,05–0,3 ms** (Faktor ~500–1000).
- Mit `ORDER BY created_at DESC` wählt der Planer ebenfalls den Bitmap-Scan plus kleinen Sort (12 Zeilen).
  Ein `Index Scan Backward` (ohne Sort) ist laut Kostenmodell minimal teurer (52,6 vs. 51,6) – mit
  `enable_bitmapscan = off` sieht man ihn.
- Größe: Tabelle ≈ 67 MB, Index ≈ 30 MB.
- Nur `created_at`: `Parallel Seq Scan` (≈ 100 ms); mit eigenem Index auf `created_at`: ≈ 2 ms.
  PostgreSQL 18 kann per **Skip Scan** einen Index auch ohne linke Spalte nutzen – aber nur, wenn die linke
  Spalte wenige Werte hat (Gegenprobe `(status, created_at)`: „Index Searches: 9“). Bei 83.333 Kunden lohnt das nicht.
- `public.orders` hat trotz `REFERENCES customers` nur `orders_pkey` – kein Index auf `customer_id`.
