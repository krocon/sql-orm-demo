-- Folge 001 – „Alles laden, im Code filtern“ vs. eine Abfrage
-- Eigene Kopie mit 2 Mio. Bestellungen im Schema ep001 (shop bleibt unberührt).
--   psql -h 127.0.0.1 -U shop -d shop -f episodes/001/vergleich.sql
SET client_min_messages = warning;
DROP SCHEMA IF EXISTS ep001 CASCADE;
CREATE SCHEMA ep001;
SET search_path = ep001, public;

CREATE TABLE orders (
  id          bigint PRIMARY KEY,
  customer_id bigint NOT NULL,
  total       numeric(10,2) NOT NULL,
  created_at  timestamptz NOT NULL
);
INSERT INTO orders
SELECT g, 1 + (g::bigint * 7919) % 1000,
       round((5 + (g * 37 % 9500) / 100.0)::numeric, 2),
       timestamptz '2024-01-01' + (g % 1000) * interval '1 day'
FROM generate_series(1, 2000000) g;
ANALYZE orders;

\timing on
\echo '--- Problem: alle 2 Mio. Zeilen zum Client schicken (wie findAll()) ---'
-- Ausgabe nach /dev/null: gemessen wird nur Lesen + Übertragen, die Java-Schleife käme noch dazu.
\o /dev/null
SELECT id, customer_id, total, created_at FROM orders;
\o

\echo '--- Lösung: die Datenbank filtert und summiert, 10 Zeilen kommen an ---'
SELECT customer_id, SUM(total) AS revenue
FROM orders
WHERE created_at >= DATE '2026-01-01'
GROUP BY customer_id
ORDER BY revenue DESC
LIMIT 10;
\timing off
