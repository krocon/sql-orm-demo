-- Folge 001 – Top-10-Kunden 2026 nach Umsatz (Code-Fenster „Hook“)
-- Filtern, Summieren, Sortieren: alles in der Datenbank, eine Abfrage.
SELECT c.name, SUM(o.total) AS revenue
FROM customers c
JOIN orders o ON o.customer_id = c.id
WHERE o.created_at >= DATE '2026-01-01'
GROUP BY c.name
ORDER BY revenue DESC
LIMIT 10;
