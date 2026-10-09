-- Folge 006 – Spaltenreihenfolge: (customer_id, created_at) hilft nur „von links“
-- (braucht 01_seed.sql und 02_index.sql)
SET search_path = ep006, public;

-- ✔ Suche nach Kunde (linke Spalte) → Index Scan
EXPLAIN (COSTS OFF)
SELECT id, total FROM orders WHERE customer_id = 42;

-- ✔ Kunde + Datum → beide Index-Spalten als Zugriffsbedingung (Index Cond)
EXPLAIN (COSTS OFF)
SELECT id, total FROM orders
WHERE customer_id = 42 AND created_at >= DATE '2026-01-01';

-- ✘ Nur Datum (rechte Spalte) → Index (customer_id, created_at) wird nicht genutzt, Seq Scan
EXPLAIN (ANALYZE, COSTS OFF)
SELECT id, total FROM orders
WHERE created_at >= DATE '2025-03-15' AND created_at < DATE '2025-03-16';

-- Hinweis PostgreSQL 18: „Skip Scan“ kann einen Index auch ohne die linke Spalte nutzen –
-- aber nur, wenn die linke Spalte wenige verschiedene Werte hat. Hier: 83.333 Kunden → lohnt nicht.
-- Gegenprobe mit status (4 Werte) vorn: Index wird genutzt, „Index Searches“ > 1.
CREATE INDEX idx_orders_status_created ON orders (status, created_at);
EXPLAIN (ANALYZE, COSTS OFF)
SELECT id, total FROM orders
WHERE created_at >= DATE '2025-03-15' AND created_at < DATE '2025-03-16';
DROP INDEX idx_orders_status_created;

-- ✔ Für Datum allein braucht es einen eigenen Index mit created_at vorn
CREATE INDEX idx_orders_created ON orders (created_at);
EXPLAIN (ANALYZE, COSTS OFF)
SELECT id, total FROM orders
WHERE created_at >= DATE '2025-03-15' AND created_at < DATE '2025-03-16';
DROP INDEX idx_orders_created;

-- Fremdschlüssel bekommen in PostgreSQL KEINEN automatischen Index:
-- shop.orders.customer_id REFERENCES customers(id) → nur orders_pkey vorhanden
SELECT indexname FROM pg_indexes WHERE schemaname = 'public' AND tablename = 'orders';
