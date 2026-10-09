-- Testdaten (wie schema/shop.sql)
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
