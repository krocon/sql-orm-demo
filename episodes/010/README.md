# Folge 010 – Query-Optimierung mit EXPLAIN   (EN: Query optimization with EXPLAIN)
Video: „Query-Optimierung mit EXPLAIN in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
`EXPLAIN` zeigt den Plan mit geschätzten Kosten, `EXPLAIN ANALYZE` führt die Abfrage wirklich aus und
zeigt echte Zeiten und Zeilen. Pläne liest man von innen nach außen; liegen geschätzte und echte Zeilen
weit auseinander, sind die Statistiken veraltet – dann hilft `ANALYZE`.

## Ausprobieren
```bash
psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/010/explain.sql   # Schema ep010, 1 Mio. Bestellungen (~10 s)
psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/010/stats.sql
psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/010/dml.sql
```

## Dateien
- `explain.sql` – Abfrage aus dem Video auf `ep010` (83.333 Kunden, Kunde 42 = ana@shop.de mit 12 Bestellungen, 1 Mio. Bestellungen): EXPLAIN, EXPLAIN ANALYZE ohne und mit `idx_orders_customer_created`; Gegenprobe auf den kleinen shop-Tabellen (Index nur in Transaktion).
- `stats.sql` – veraltete Statistik erzeugen (Autovacuum aus, 500.000 Zeilen nach dem letzten ANALYZE), dann `ANALYZE`.
- `dml.sql` – `EXPLAIN ANALYZE DELETE` in `BEGIN … ROLLBACK`.

## Was du sehen solltest
Werte aus unseren Läufen (PostgreSQL 18.4), gerundet:
- Ohne Index: `Gather` → `Hash Join` → `Parallel Seq Scan on orders o` + `Index Scan using customers_email_key`,
  **≈ 150–230 ms**.
- Mit Index – der echte Plan (PostgreSQL 18 zeigt bei ANALYZE auch Buffers und „Index Searches“):
  ```
  Nested Loop (actual rows=12.00 loops=1)
    ->  Index Scan using customers_email_key on customers c (actual rows=1.00 loops=1)
          Index Cond: (email = 'ana@shop.de'::text)
    ->  Bitmap Heap Scan on orders o (actual rows=12.00 loops=1)
          Recheck Cond: (customer_id = c.id)
          ->  Bitmap Index Scan on idx_orders_customer_created (actual rows=12.00 loops=1)
                Index Cond: (customer_id = c.id)
  ```
  **≈ 0,05–0,1 ms**. Auf orders greift PostgreSQL per Bitmap-Scan zu (12 Zeilen auf 12 Seiten), nicht per
  einfachem `Index Scan`. Auf den kleinen shop-Tabellen derselbe Plan mit `rows=0` (Ana hat dort keine Bestellung).
- `stats.sql`: vor ANALYZE für `status = 'NEW'` geschätzt ≈ 450.000, tatsächlich 1.000; für `'PAID'` geschätzt 1,
  tatsächlich 500.000. Nach `ANALYZE`: ≈ 1.000 bzw. ≈ 500.000 – Schätzung passt.
- `dml.sql`: `DELETE` von 1.000 stornierten Bestellungen läuft wirklich (in der Transaktion danach 0),
  nach `ROLLBACK` wieder 1.000. Überraschung im Plan: `Trigger for constraint order_items_order_id_fkey:
  time ≈ 1.500 ms` – das `ON DELETE CASCADE` sucht 1.000-mal in `order_items` ohne Index auf `order_id`
  (siehe Folge 006: Fremdschlüssel bekommen keinen automatischen Index).
