-- Folge 007 – die Transaktion aus dem Video (Code-Fenster „Hook“): beides oder nichts

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

SELECT id, stock FROM products WHERE id = 7;            -- vorher: 165

BEGIN;
UPDATE products SET stock = stock - 1 WHERE id = 7;
-- unit_price ist NOT NULL (Preis zum Bestellzeitpunkt, siehe Folge 008) → muss mit rein
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
  VALUES (1001, 7, 1, (SELECT price FROM products WHERE id = 7));
COMMIT;  -- beides oder nichts

SELECT id, stock FROM products WHERE id = 7;            -- nachher: 164
SELECT order_id, product_id, quantity, unit_price FROM order_items;   -- 1 neue Position
