-- Folge 010 – der Plan aus dem Video (Code-Fenster „Hook“), gemessen auf 1 Mio. Bestellungen
-- Eigenes Schema ep010: 83.333 Kunden (Kunde 42 = ana@shop.de) mit je ~12 Bestellungen.
SET client_min_messages = warning;
DROP SCHEMA IF EXISTS ep010 CASCADE;
CREATE SCHEMA ep010;
SET search_path = ep010, public;

CREATE TABLE customers (
  id    bigint PRIMARY KEY,
  email text NOT NULL UNIQUE,                 -- → Index „customers_email_key“
  name  text NOT NULL
);
INSERT INTO customers
SELECT g, CASE g WHEN 42 THEN 'ana@shop.de' ELSE 'kunde' || g || '@shop.example' END,
       CASE g WHEN 42 THEN 'Ana Beispiel' ELSE 'Kunde ' || g END
FROM generate_series(1, 83333) g;

CREATE TABLE orders (
  id          bigint PRIMARY KEY,
  customer_id bigint NOT NULL,                -- verweist auf customers.id (FK weggelassen: schnelleres Laden)
  status      text NOT NULL,
  total       numeric(10,2) NOT NULL,
  created_at  timestamptz NOT NULL
);
INSERT INTO orders
SELECT g, 1 + (g::bigint * 7919) % 83333,
       (ARRAY['NEW','PAID','PAID','SHIPPED','CANCELLED'])[1 + g % 5],
       round((5 + (g::bigint * 37 % 49500) / 100.0)::numeric, 2),
       timestamptz '2024-01-01' + (g % 1000) * interval '1 day' + (g % 86400) * interval '1 second'
FROM generate_series(1, 1000000) g;
VACUUM ANALYZE customers, orders;

-- 1) EXPLAIN ohne ANALYZE: nur Plan + geschätzte Kosten, nichts wird ausgeführt
EXPLAIN
SELECT o.id, o.total FROM orders o
JOIN customers c ON c.id = o.customer_id
WHERE c.email = 'ana@shop.de';

-- 2) Ohne Index auf orders.customer_id: Seq Scan über 1 Mio. Zeilen
EXPLAIN (ANALYZE, COSTS OFF)
SELECT o.id, o.total FROM orders o
JOIN customers c ON c.id = o.customer_id
WHERE c.email = 'ana@shop.de';

-- 3) Index aus Folge 006 anlegen …
CREATE INDEX idx_orders_customer_created ON orders (customer_id, created_at);
ANALYZE orders;

-- … und die Abfrage aus dem Video erneut messen
EXPLAIN ANALYZE
SELECT o.id, o.total FROM orders o
JOIN customers c ON c.id = o.customer_id
WHERE c.email = 'ana@shop.de';

-- kompakt (ohne Kosten/Zeiten) – so sieht der Plan fürs Code-Fenster aus
EXPLAIN (ANALYZE, COSTS OFF, TIMING OFF, SUMMARY OFF, BUFFERS OFF)
SELECT o.id, o.total FROM orders o
JOIN customers c ON c.id = o.customer_id
WHERE c.email = 'ana@shop.de';

-- 4) Gegenprobe auf den kleinen shop-Tabellen (Ana hat dort keine Bestellung → rows=0),
--    Index nur in der Transaktion, danach ROLLBACK
BEGIN;
CREATE INDEX idx_orders_customer_created ON public.orders (customer_id, created_at);
ANALYZE public.orders;
EXPLAIN (ANALYZE, COSTS OFF, TIMING OFF, SUMMARY OFF, BUFFERS OFF)
SELECT o.id, o.total FROM public.orders o
JOIN public.customers c ON c.id = o.customer_id
WHERE c.email = 'ana@shop.de';
ROLLBACK;
