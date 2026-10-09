-- Folge 006 – große Bestelltabelle: 1 Mio. Zeilen im eigenen Schema ep006 (shop bleibt unberührt)
-- ca. 83.000 Kunden mit je ~12 Bestellungen, Daten über 3 Jahre verteilt.
SET client_min_messages = warning;
DROP SCHEMA IF EXISTS ep006 CASCADE;
CREATE SCHEMA ep006;
SET search_path = ep006, public;

CREATE TABLE orders (
  id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  customer_id bigint NOT NULL,          -- bewusst OHNE Index (wie ein Fremdschlüssel in PostgreSQL)
  status      text NOT NULL,
  total       numeric(10,2) NOT NULL,
  created_at  timestamptz NOT NULL
);

INSERT INTO orders (customer_id, status, total, created_at)
SELECT 1 + (g::bigint * 7919) % 83333,
       (ARRAY['NEW','PAID','PAID','SHIPPED','CANCELLED'])[1 + g % 5],
       round((5 + (g::bigint * 37 % 49500) / 100.0)::numeric, 2),
       timestamptz '2024-01-01' + (g % 1000) * interval '1 day' + (g % 86400) * interval '1 second'
FROM generate_series(1, 1000000) g;

ANALYZE orders;

SELECT count(*) AS bestellungen, count(DISTINCT customer_id) AS kunden FROM orders;
