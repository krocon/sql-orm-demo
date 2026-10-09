-- Folge 010 – Vorsicht: EXPLAIN ANALYZE führt UPDATE/DELETE wirklich aus!
-- Deshalb immer in BEGIN … ROLLBACK verpacken.
SELECT count(*) AS stornierte_vorher FROM orders WHERE status = 'CANCELLED';   -- 1000

BEGIN;
EXPLAIN (ANALYZE, COSTS OFF)
DELETE FROM orders WHERE status = 'CANCELLED';
-- Plan zeigt den Seq Scan (1000 Zeilen) und zwei Zeilen „Trigger for constraint
-- order_items_order_id_fkey“: das ON DELETE CASCADE löscht auch 2000 Positionen.
-- Der Cascade-Trigger braucht ~1,5 s, weil order_items.order_id KEINEN Index hat
-- (Fremdschlüssel bekommen keinen automatisch, siehe Folge 006) → 1000 × Seq Scan.

-- In dieser Transaktion sind die Zeilen wirklich weg:
SELECT count(*) AS stornierte_in_tx FROM orders WHERE status = 'CANCELLED';   -- 0
ROLLBACK;

-- Nach dem ROLLBACK ist alles wieder da
SELECT count(*) AS stornierte_nachher FROM orders WHERE status = 'CANCELLED';  -- 1000
SELECT count(*) AS positionen FROM order_items;                                -- 10000

-- Nur den Plan sehen, ohne auszuführen: EXPLAIN ohne ANALYZE
EXPLAIN DELETE FROM orders WHERE status = 'CANCELLED';
