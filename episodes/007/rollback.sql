-- Folge 007 – Atomicity + Consistency: CHECK-Verletzung → die ganze Transaktion wird zurückgerollt
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

SELECT id, stock FROM products WHERE id IN (7, 8) ORDER BY id;   -- vorher: 165 | 39

BEGIN;
-- 1) gültig: Produkt 8 um 1 verringern
UPDATE products SET stock = stock - 1 WHERE id = 8;
-- 2) ungültig: Produkt 7 ins Minus → CHECK (stock >= 0) schlägt zu
UPDATE products SET stock = stock - 1000 WHERE id = 7;
--   ERROR:  new row for relation "products" violates check constraint "products_stock_check"
-- 3) Danach nimmt PostgreSQL in dieser Transaktion nichts mehr an:
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (1001, 8, 1, 9.99);
--   ERROR:  current transaction is aborted, commands ignored until end of transaction block
ROLLBACK;   -- (ein COMMIT würde hier ebenfalls nur ROLLBACK melden)

-- Auch die gültige Änderung an Produkt 8 ist weg: alles oder nichts
SELECT id, stock FROM products WHERE id IN (7, 8) ORDER BY id;   -- nachher: 165 | 39
SELECT count(*) AS positionen FROM order_items;                   -- 0

\set ON_ERROR_STOP 1

-- Dieselbe Logik als DO-Block: Fehler abfangen, Transaktion des Blocks wird zurückgesetzt
DO $$
BEGIN
  UPDATE products SET stock = stock - 1    WHERE id = 8;
  UPDATE products SET stock = stock - 1000 WHERE id = 7;
EXCEPTION WHEN check_violation THEN
  RAISE NOTICE 'CHECK verletzt (%), beide Updates zurückgerollt', SQLERRM;
END $$;
SELECT id, stock FROM products WHERE id IN (7, 8) ORDER BY id;   -- immer noch 165 | 39
