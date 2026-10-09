-- Folge 034: Die Datenbank fragen – EXPLAIN ANALYZE vor und nach einem Index.
-- Läuft in einer Transaktion mit ROLLBACK: der Index ist danach wieder weg.
-- Ausführen (Repo-Wurzel): docker compose exec -T db psql -U shop -d shop < episodes/034/explain.sql
BEGIN;

-- Bestellungen eines Kunden (die Query hinter o.getCustomer().getOrders() bzw. "Meine Bestellungen")
EXPLAIN (ANALYZE, COSTS OFF, TIMING OFF, SUMMARY OFF)
SELECT id, status, total FROM orders WHERE customer_id = 42;

CREATE INDEX orders_customer_id_idx ON orders (customer_id);
ANALYZE orders;

EXPLAIN (ANALYZE, COSTS OFF, TIMING OFF, SUMMARY OFF)
SELECT id, status, total FROM orders WHERE customer_id = 42;

ROLLBACK;
