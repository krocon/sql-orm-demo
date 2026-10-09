-- Folge 004 – absichtlicher Fehler: nicht gruppierte Spalte im SELECT (Code-Fenster „Problem“)
-- ON_ERROR_STOP aus, damit scripts/run-all.sh hier nicht abbricht.
\set ON_ERROR_STOP 0

-- ✘ ERROR:  column "customers.name" must appear in the GROUP BY clause
--           or be used in an aggregate function
SELECT country, name, COUNT(*)
FROM customers
GROUP BY country;

\set ON_ERROR_STOP 1

-- ✔ Entweder aggregieren …
SELECT country, MIN(name) AS erster_name, COUNT(*)
FROM customers
GROUP BY country;

-- ✔ … oder mitgruppieren (dann eine Zeile pro Land UND Name)
SELECT country, name, COUNT(*)
FROM customers
GROUP BY country, name
ORDER BY country, name
LIMIT 5;
