-- Folge 003 – INNER, LEFT und RIGHT JOIN auf customers/orders

-- INNER JOIN: nur Kunden MIT Bestellung → 5000 Zeilen, Ana fehlt
SELECT count(*) AS inner_zeilen
FROM customers c
JOIN orders o ON o.customer_id = c.id;

-- LEFT JOIN (Code-Fenster „Hook“): alle Kunden, fehlt rechts ein Partner → NULL
SELECT c.name, o.id AS order_id, o.total
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.id
ORDER BY c.name;
-- → 5001 Zeilen: 5000 Bestellungen + „Ana Beispiel“ mit order_id = NULL

-- Typische Frage: Wer hat nie bestellt?
SELECT c.name, c.email
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.id
WHERE o.id IS NULL;
-- → 1 Zeile: Ana Beispiel, ana@shop.de

-- RIGHT JOIN: Spiegelbild – gleiches Ergebnis wie oben, nur Tabellen getauscht
SELECT c.name, c.email
FROM orders o
RIGHT JOIN customers c ON o.customer_id = c.id
WHERE o.id IS NULL;
