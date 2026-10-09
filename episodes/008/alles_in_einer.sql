-- Folge 008 – alles in einer Tabelle (Code-Fenster „Problem“) und die Folgen
SET client_min_messages = warning;
DROP SCHEMA IF EXISTS ep008 CASCADE;
CREATE SCHEMA ep008;
SET search_path = ep008, public;

CREATE TABLE orders_flat (
  id             int PRIMARY KEY,
  customer_email text,
  city           text,
  products       text          -- Liste in einer Zelle → verletzt die 1. Normalform
);
INSERT INTO orders_flat VALUES
  (1, 'ana@shop.de', 'Hamburg', 'Tasse, Shirt'),
  (2, 'ana@shop.de', 'Hambrug', 'Poster'),      -- Tippfehler: niemand merkt es
  (3, 'ana@shop.de', 'Hamburg', 'Tasse');

TABLE orders_flat;

-- Anomalie 1: In welcher Stadt wohnt Ana? Zwei Antworten.
SELECT DISTINCT city FROM orders_flat WHERE customer_email = 'ana@shop.de' ORDER BY city;

-- Anomalie 2 (Update): Ana zieht nach Berlin – wer nur „Hamburg“ ersetzt, vergisst „Hambrug“
UPDATE orders_flat SET city = 'Berlin' WHERE city = 'Hamburg';
SELECT DISTINCT city FROM orders_flat WHERE customer_email = 'ana@shop.de' ORDER BY city;
-- → Berlin UND Hambrug

-- Anomalie 3: Listen in Zellen lassen sich kaum abfragen. Wie oft wurde „Tasse“ bestellt?
SELECT count(*) AS mit_like FROM orders_flat WHERE products LIKE '%Tasse%';  -- 2, ok …
-- … aber schon „Tassenhalter“ würde mitgezählt; richtig geht es erst nach dem Zerlegen:

-- 1. Normalform: eine Zeile pro Bestellposition
SELECT id AS order_id, trim(p) AS product
FROM orders_flat, string_to_table(products, ',') AS p
ORDER BY id, product;
