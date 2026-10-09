-- Gemeinsames Beispiel-Schema „Online-Shop“ für alle Folgen der Serie
-- „SQL, ORM & Hibernate in 60 Sekunden“. Wiederholbar: löscht und erzeugt alles neu.
--   psql -h 127.0.0.1 -p 5433 -U shop -d shop -f schema/shop.sql
DROP TABLE IF EXISTS order_items, orders, products, categories, customers CASCADE;

CREATE TABLE customers (
  id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  email      text NOT NULL UNIQUE,
  name       text NOT NULL,
  country    text NOT NULL,
  city       text,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE categories (
  id        bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  parent_id bigint REFERENCES categories (id),
  name      text NOT NULL
);

CREATE TABLE products (
  id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  sku         text NOT NULL UNIQUE,
  name        text NOT NULL,
  category_id bigint REFERENCES categories (id),
  price       numeric(10,2) NOT NULL CHECK (price >= 0),
  stock       integer NOT NULL CHECK (stock >= 0)
);

CREATE TABLE orders (
  id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  customer_id bigint NOT NULL REFERENCES customers (id),
  status      text NOT NULL DEFAULT 'NEW'
              CHECK (status IN ('NEW', 'PAID', 'SHIPPED', 'CANCELLED')),
  total       numeric(10,2) NOT NULL DEFAULT 0,
  created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE order_items (
  id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id   bigint NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
  product_id bigint NOT NULL REFERENCES products (id),
  quantity   integer NOT NULL CHECK (quantity > 0),
  unit_price numeric(10,2) NOT NULL   -- Preis zum Bestellzeitpunkt (Historie, keine Redundanz)
);

-- ───────────── Testdaten (deterministisch) ─────────────
SELECT setseed(0.42);

INSERT INTO customers (email, name, country, city, created_at)
SELECT 'kunde' || g || '@shop.example',
       'Kunde ' || g,
       (ARRAY['DE','DE','DE','AT','CH','NL','FR'])[1 + g % 7],
       (ARRAY['Hamburg','Berlin','München','Wien','Zürich','Amsterdam','Paris'])[1 + g % 7],
       timestamptz '2025-01-01' + (g % 600) * interval '1 day'
FROM generate_series(1, 999) g;
-- Kundin aus den Videos (ohne Bestellung → LEFT JOIN zeigt sie mit NULL)
INSERT INTO customers (email, name, country, city, created_at)
VALUES ('ana@shop.de', 'Ana Beispiel', 'DE', 'Hamburg', timestamptz '2026-03-01');

INSERT INTO categories (parent_id, name) VALUES
  (NULL, 'Küche'), (1, 'Tassen'), (1, 'Teller'), (NULL, 'Kleidung'), (4, 'Shirts'), (5, 'Bio-Shirts');

INSERT INTO products (sku, name, category_id, price, stock)
SELECT CASE g WHEN 1 THEN 'MUG-1' ELSE 'SKU-' || lpad(g::text, 3, '0') END,
       CASE g WHEN 1 THEN 'Tasse' ELSE 'Produkt ' || g END,
       1 + g % 6,
       round((5 + random() * 95)::numeric, 2),
       (random() * 200)::int
FROM generate_series(1, 50) g;

INSERT INTO orders (customer_id, status, created_at)
SELECT 1 + (g * 7919) % 999,
       (ARRAY['NEW','PAID','PAID','SHIPPED','CANCELLED'])[1 + g % 5],
       timestamptz '2025-06-01' + (g % 480) * interval '1 day' + (g % 24) * interval '1 hour'
FROM generate_series(1, 5000) g;

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
SELECT o.id, p.id, 1 + (o.id + n) % 3, p.price
FROM orders o
CROSS JOIN generate_series(1, 2) n
JOIN products p ON p.id = 1 + (o.id * 31 + n * 17) % 50;

UPDATE orders o SET total = s.sum
FROM (SELECT order_id, SUM(quantity * unit_price) AS sum FROM order_items GROUP BY order_id) s
WHERE s.order_id = o.id;

ANALYZE;
