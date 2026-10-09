-- Folge 007 – Auto-Commit (Code-Fenster „Problem“) vs. Transaktion
-- Den „Absturz“ zwischen den Anweisungen simulieren wir mit einem Fehler im INSERT
-- (unit_price fehlt). Für einen echten Absturz mit zwei psql-Sitzungen: siehe README.
-- Der Fehler ist Absicht: ON_ERROR_STOP aus, damit scripts/run-all.sh weiterläuft.

-- Kopien von products/order_items im Schema ep007, damit die Demo die shop-Daten
-- der anderen Folgen nicht verändert („products“/„order_items“ = Kopien, „orders“ = shop).
SET client_min_messages = warning;
DROP SCHEMA IF EXISTS ep007 CASCADE;
CREATE SCHEMA ep007;
CREATE TABLE ep007.products (LIKE public.products INCLUDING ALL);   -- inkl. CHECK (stock >= 0)
INSERT INTO ep007.products OVERRIDING SYSTEM VALUE SELECT * FROM public.products;
CREATE TABLE ep007.order_items (LIKE public.order_items INCLUDING ALL);
ALTER TABLE ep007.order_items ADD FOREIGN KEY (product_id) REFERENCES ep007.products (id);
SET search_path = ep007, public;
RESET client_min_messages;
\set ON_ERROR_STOP 0

\echo '--- ✘ Auto-Commit: jede Anweisung ist eine eigene Transaktion'
UPDATE products SET stock = stock - 1 WHERE id = 7;            -- sofort festgeschrieben
INSERT INTO order_items (order_id, product_id, quantity)       -- schlägt fehl
  VALUES (1001, 7, 1);
SELECT id, stock FROM products WHERE id = 7;                   -- 164: Ware weg …
SELECT count(*) AS positionen FROM order_items;                -- 0: … aber keine Bestellung

-- Zustand zurücksetzen
TRUNCATE order_items;
UPDATE products p SET stock = s.stock FROM public.products s WHERE s.id = p.id;

\echo '--- ✔ Transaktion: der Fehler rollt auch das UPDATE zurück'
BEGIN;
UPDATE products SET stock = stock - 1 WHERE id = 7;
INSERT INTO order_items (order_id, product_id, quantity)
  VALUES (1001, 7, 1);
ROLLBACK;
SELECT id, stock FROM products WHERE id = 7;                   -- 165: nichts passiert
SELECT count(*) AS positionen FROM order_items;                -- 0

\set ON_ERROR_STOP 1
