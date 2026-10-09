-- Folge 009 – relationales Bestellmodell (Code-Fenster „Hook“, oberer Teil)
-- Normalisiert: Bestellung, Positionen, Produkte, Kunden in eigenen Tabellen, JOIN bei Bedarf.

-- Abfrage aus dem Video, eingeschränkt auf eine Bestellung (sonst 10.000 Zeilen)
SELECT o.id, i.product_id, i.quantity
FROM orders o JOIN order_items i ON i.order_id = o.id
WHERE o.id = 1001
ORDER BY i.product_id;

-- Flexibel abfragen: dieselben Tabellen beantworten ganz andere Fragen,
-- z. B. „Welche Produkte verkaufen sich pro Land am besten?“
SELECT c.country, p.sku, sum(i.quantity) AS stueck
FROM order_items i
JOIN orders o    ON o.id = i.order_id
JOIN customers c ON c.id = o.customer_id
JOIN products p  ON p.id = i.product_id
GROUP BY c.country, p.sku
ORDER BY stueck DESC
LIMIT 5;

-- Und genau diese Bestellung „so, wie man sie liest“ – als Dokument zusammengebaut
SELECT jsonb_pretty(jsonb_build_object(
         '_id', o.id,
         'customer', c.email,
         'items', jsonb_agg(jsonb_build_object('sku', p.sku, 'qty', i.quantity) ORDER BY p.sku)))
       AS dokument
FROM orders o
JOIN customers c   ON c.id = o.customer_id
JOIN order_items i ON i.order_id = o.id
JOIN products p    ON p.id = i.product_id
WHERE o.id = 1001
GROUP BY o.id, c.email;
