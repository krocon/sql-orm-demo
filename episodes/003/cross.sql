-- Folge 003 – Join-Bedingung vergessen = Kreuzprodukt (Code-Fenster „Problem“)
-- FROM a, b ohne WHERE/ON verbindet jede Zeile mit jeder.
SELECT (SELECT count(*) FROM customers) AS kunden,
       (SELECT count(*) FROM orders)    AS bestellungen;

-- 1.000 × 5.000 = 5.000.000 Zeilen
SELECT count(*) AS zeilen
FROM customers c, orders o;

-- Mit Bedingung: 5.000 Zeilen (eine pro Bestellung)
SELECT count(*) AS zeilen
FROM customers c
JOIN orders o ON o.customer_id = c.id;
