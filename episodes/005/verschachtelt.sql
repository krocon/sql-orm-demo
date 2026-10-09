-- Folge 005 – drei verschachtelte Subqueries (Code-Fenster „Problem“) vs. dieselbe Abfrage als CTE
-- Frage: Welche Kunden hatten eine Bestellung über dem Durchschnitt der letzten 30 Tage?

-- ✘ Von innen nach außen lesen …
SELECT name FROM customers WHERE id IN (
  SELECT customer_id FROM orders WHERE total > (
    SELECT AVG(total) FROM orders WHERE created_at > (
      SELECT MAX(created_at) - INTERVAL '30 days' FROM orders)))
ORDER BY name;

-- ✔ Von oben nach unten lesen: jeder Zwischenschritt hat einen Namen
WITH stichtag AS (
  SELECT MAX(created_at) - INTERVAL '30 days' AS ab FROM orders
), schnitt AS (
  SELECT AVG(total) AS wert
  FROM orders, stichtag
  WHERE created_at > stichtag.ab
), gute_kunden AS (
  SELECT customer_id
  FROM orders, schnitt
  WHERE total > schnitt.wert
)
SELECT name FROM customers
WHERE id IN (SELECT customer_id FROM gute_kunden)
ORDER BY name;

-- Beweis „gleiches Ergebnis“: beide Differenzen sind leer → 888 | 888 | 0
WITH a AS (
  SELECT name FROM customers WHERE id IN (
    SELECT customer_id FROM orders WHERE total > (
      SELECT AVG(total) FROM orders WHERE created_at > (
        SELECT MAX(created_at) - INTERVAL '30 days' FROM orders)))
), stichtag AS (
  SELECT MAX(created_at) - INTERVAL '30 days' AS ab FROM orders
), schnitt AS (
  SELECT AVG(total) AS wert FROM orders, stichtag WHERE created_at > stichtag.ab
), b AS (
  SELECT name FROM customers
  WHERE id IN (SELECT customer_id FROM orders, schnitt WHERE total > schnitt.wert)
)
SELECT (SELECT count(*) FROM a) AS verschachtelt,
       (SELECT count(*) FROM b) AS cte,
       (SELECT count(*) FROM ((TABLE a EXCEPT TABLE b) UNION ALL (TABLE b EXCEPT TABLE a)) d) AS unterschiede;
