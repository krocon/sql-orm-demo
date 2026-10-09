-- Folge 008 – normalisiertes Schema (Code-Fenster „Hook“, ergänzt um Produkte und Positionen)
-- Das vollständige Shop-Schema aller Folgen steht in schema/shop.sql – hier die Kurzfassung
-- im eigenen Schema ep008n, damit shop unberührt bleibt.
SET client_min_messages = warning;
DROP SCHEMA IF EXISTS ep008n CASCADE;
CREATE SCHEMA ep008n;
SET search_path = ep008n;
RESET client_min_messages;

-- aus dem Video
CREATE TABLE customers (
  id    bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  email text NOT NULL UNIQUE);
CREATE TABLE orders (
  id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  customer_id bigint NOT NULL REFERENCES customers (id),
  total       numeric(10,2) NOT NULL);

-- Kundendaten (Stadt) gehören an den Kunden – 3. Normalform: hängt nur am Schlüssel des Kunden
ALTER TABLE customers ADD COLUMN city text;

-- 1. Normalform: keine Liste in einer Zelle, sondern eine Zeile pro Position
CREATE TABLE products (
  id    bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name  text NOT NULL UNIQUE,
  price numeric(10,2) NOT NULL);
CREATE TABLE order_items (
  order_id   bigint NOT NULL REFERENCES orders (id),
  product_id bigint NOT NULL REFERENCES products (id),
  quantity   integer NOT NULL CHECK (quantity > 0),
  unit_price numeric(10,2) NOT NULL,          -- Historie: Preis zum Bestellzeitpunkt
  PRIMARY KEY (order_id, product_id));

-- Dieselben Daten wie in alles_in_einer.sql – Ana und ihre Stadt gibt es genau einmal
INSERT INTO customers (email, city) VALUES ('ana@shop.de', 'Hamburg');
INSERT INTO products (name, price) VALUES ('Tasse', 9.90), ('Shirt', 19.90), ('Poster', 14.90);
INSERT INTO orders (customer_id, total) VALUES (1, 29.80), (1, 14.90), (1, 9.90);
INSERT INTO order_items VALUES (1, 1, 1, 9.90), (1, 2, 1, 19.90), (2, 3, 1, 14.90), (3, 1, 1, 9.90);

-- Umzug = genau EINE Zeile ändern
UPDATE customers SET city = 'Berlin' WHERE email = 'ana@shop.de';
SELECT o.id, c.email, c.city FROM orders o JOIN customers c ON c.id = o.customer_id ORDER BY o.id;

-- Wie oft wurde „Tasse“ bestellt? Exakt, ohne LIKE
SELECT p.name, count(*) AS bestellungen
FROM order_items i JOIN products p ON p.id = i.product_id
GROUP BY p.name ORDER BY p.name;

-- REFERENCES verhindert Verweise ins Leere: Kunde 99 gibt es nicht
DO $$
BEGIN
  INSERT INTO orders (customer_id, total) VALUES (99, 1.00);
EXCEPTION WHEN foreign_key_violation THEN
  RAISE NOTICE 'abgelehnt: %', SQLERRM;
END $$;
