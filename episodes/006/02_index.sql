-- Folge 006 – EXPLAIN ANALYZE vorher/nachher (braucht 01_seed.sql)
SET search_path = ep006, public;
SET client_min_messages = warning;
DROP INDEX IF EXISTS idx_orders_customer_created;
RESET client_min_messages;

-- ✘ Vorher (Code-Fenster „Problem“): kein Index auf customer_id → Seq Scan über 1 Mio. Zeilen
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id, total FROM orders WHERE customer_id = 42;

SELECT count(*) AS treffer FROM orders WHERE customer_id = 42;   -- 12 Treffer

-- ✔ Index auf (customer_id, created_at) (Code-Fenster „Hook“)
CREATE INDEX idx_orders_customer_created
  ON orders (customer_id, created_at);

-- Nachher: Zugriff über den Index, nur noch die 12 passenden Zeilen.
-- PostgreSQL wählt hier „Bitmap Index Scan“ + „Bitmap Heap Scan“ (12 Zeilen auf 12 verschiedenen
-- Seiten); ein klassischer „Index Scan“ wäre laut Kostenmodell minimal teurer.
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id, total FROM orders WHERE customer_id = 42;

-- Abfrage aus dem Video mit ORDER BY created_at DESC: real ebenfalls Bitmap-Scan + kleiner Sort
-- (12 Zeilen, quicksort im Speicher).
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id, total FROM orders
WHERE customer_id = 42
ORDER BY created_at DESC;

-- Zum Vergleich: Bitmap-Scans aus → „Index Scan Backward using idx_orders_customer_created“
-- (der Index liefert die Zeilen schon sortiert, kein Sort-Knoten)
SET enable_bitmapscan = off;
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF)
SELECT id, total FROM orders
WHERE customer_id = 42
ORDER BY created_at DESC;
RESET enable_bitmapscan;

-- Kosten: der Index braucht Platz
SELECT pg_size_pretty(pg_relation_size('orders'))                       AS tabelle,
       pg_size_pretty(pg_relation_size('idx_orders_customer_created')) AS index;
