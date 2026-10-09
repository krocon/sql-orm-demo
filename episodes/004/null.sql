-- Folge 004 – NULL-Fallen bei Aggregaten

-- COUNT(*) zählt Zeilen, COUNT(spalte) überspringt NULL.
-- Im LEFT JOIN hat Ana keine Bestellung: o.id ist NULL.
SELECT COUNT(*)    AS zeilen,          -- 5001 (inkl. Ana mit NULL-Zeile)
       COUNT(o.id) AS bestellungen     -- 5000
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.id;

-- Pro Kunde: Ana hat 1 Zeile, aber 0 Bestellungen
SELECT c.name, COUNT(*) AS zeilen, COUNT(o.id) AS bestellungen, SUM(o.total) AS umsatz
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.id
WHERE c.email = 'ana@shop.de'
GROUP BY c.name;
-- → Ana Beispiel | 1 | 0 | NULL

-- email ist NOT NULL → hier sind beide gleich (1000 | 1000)
SELECT COUNT(*) AS zeilen, COUNT(email) AS mit_email FROM customers;

-- SUM über keine Zeilen liefert NULL, nicht 0 …
SELECT SUM(total) AS summe, COUNT(*) AS anzahl
FROM orders
WHERE created_at >= DATE '2030-01-01';
-- → summe = NULL, anzahl = 0

-- … COALESCE macht daraus 0
SELECT COALESCE(SUM(total), 0) AS summe
FROM orders
WHERE created_at >= DATE '2030-01-01';
