-- Folge 002 – NULL prüft man mit IS NULL, nicht mit =
-- shop.customers.email ist NOT NULL; daher eine kleine Kopie mit einer fehlenden E-Mail.
SET client_min_messages = warning;
DROP SCHEMA IF EXISTS ep002 CASCADE;
CREATE SCHEMA ep002;
SET search_path = ep002, public;

CREATE TABLE kontakte (name text NOT NULL, email text);
INSERT INTO kontakte VALUES
  ('Ana Beispiel', 'ana@shop.de'),
  ('Ben Ohnemail', NULL),
  ('Cem Kunde',    'cem@shop.de');

-- 1) = NULL ergibt nie TRUE, sondern NULL (unbekannt) → 0 Zeilen
SELECT name FROM kontakte WHERE email = NULL;

-- 2) richtig: IS NULL → 1 Zeile (Ben Ohnemail)
SELECT name FROM kontakte WHERE email IS NULL;

-- 3) Achtung auch bei <>: Zeilen mit NULL fallen ebenfalls raus → nur Cem
SELECT name FROM kontakte WHERE email <> 'ana@shop.de';

-- 4) NULL-sicherer Vergleich: IS DISTINCT FROM → Ben und Cem
SELECT name FROM kontakte WHERE email IS DISTINCT FROM 'ana@shop.de';

-- 5) Im Shop: WHERE email = NULL liefert ebenfalls 0 Zeilen
SELECT count(*) AS treffer FROM public.customers WHERE email = NULL;
