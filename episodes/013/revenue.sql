-- Folge 013: Umsatz pro Land als eine Abfrage (psql gegen DB shop)
-- psql -h localhost -U shop -d shop -f episodes/013/revenue.sql
SELECT c.country, sum(o.total) AS total
FROM orders o JOIN customers c ON c.id = o.customer_id
GROUP BY ROLLUP (c.country)
ORDER BY c.country NULLS LAST;
