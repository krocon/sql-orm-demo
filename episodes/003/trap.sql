-- Folge 003 – die Filterfalle: WHERE auf die rechte Tabelle macht aus LEFT einen INNER JOIN

-- ✘ Bedingung in WHERE: Kunden ohne bezahlte Bestellung verschwinden (Ana fehlt)
SELECT count(DISTINCT c.id) AS kunden, count(*) AS zeilen
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.id
WHERE o.status = 'PAID';
-- → 999 Kunden, 2000 Zeilen

-- ✔ Bedingung in ON: alle Kunden bleiben, ohne PAID-Bestellung steht rechts NULL
SELECT count(DISTINCT c.id) AS kunden, count(*) AS zeilen
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.id
                  AND o.status = 'PAID';
-- → 1000 Kunden, 2001 Zeilen

-- Der Plan verrät es: im ersten Fall macht PostgreSQL daraus einen normalen (inner) Join
EXPLAIN (COSTS OFF)
SELECT c.name, o.id
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.id
WHERE o.status = 'PAID';
