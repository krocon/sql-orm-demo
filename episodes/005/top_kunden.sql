-- Folge 005 – Top-10-Kunden mit CTE (Code-Fenster „Hook“) vs. Subquery im FROM

-- ✔ CTE wie im Video (+ äußeres ORDER BY: ohne ORDER BY ist die Reihenfolge nicht garantiert,
--   siehe Folge 002; customer_id als Tiebreaker, weil es Gleichstände beim Umsatz gibt)
WITH top_customers AS (
  SELECT customer_id, SUM(total) AS revenue
  FROM orders GROUP BY customer_id
  ORDER BY revenue DESC, customer_id LIMIT 10
)
SELECT c.name, t.revenue
FROM top_customers t JOIN customers c ON c.id = t.customer_id
ORDER BY t.revenue DESC, c.id;

-- Dasselbe als Subquery im FROM – gleiches Ergebnis
SELECT c.name, t.revenue
FROM (SELECT customer_id, SUM(total) AS revenue
      FROM orders GROUP BY customer_id
      ORDER BY revenue DESC, customer_id LIMIT 10) t
JOIN customers c ON c.id = t.customer_id
ORDER BY t.revenue DESC, c.id;

-- Subquery bleibt ideal für kurze Prüfungen: Kunden ohne Bestellung mit NOT EXISTS → Ana
SELECT name FROM customers c
WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.customer_id = c.id);
