-- Folge 005 – Performance: PostgreSQL ≥ 12 bettet einfache CTEs in die Abfrage ein (Inlining)
-- Voraussetzung fürs Inlining: nicht rekursiv, ohne Seiteneffekte, nur EINMAL referenziert.

-- 1) Subquery im FROM
EXPLAIN (COSTS OFF)
SELECT c.name FROM customers c
WHERE c.id IN (SELECT o.customer_id FROM (SELECT * FROM orders WHERE status = 'PAID') o);

-- 2) dieselbe Abfrage mit CTE → identischer Plan, kein „CTE Scan“ (inlined)
EXPLAIN (COSTS OFF)
WITH bezahlt AS (SELECT * FROM orders WHERE status = 'PAID')
SELECT c.name FROM customers c
WHERE c.id IN (SELECT customer_id FROM bezahlt);

-- 3) Altes Verhalten (vor PG 12) erzwingen: MATERIALIZED → „CTE Scan on bezahlt“
EXPLAIN (COSTS OFF)
WITH bezahlt AS MATERIALIZED (SELECT * FROM orders WHERE status = 'PAID')
SELECT c.name FROM customers c
WHERE c.id IN (SELECT customer_id FROM bezahlt);

-- 4) Top-10 aus dem Video: CTE vs. Subquery → gleicher Plan
EXPLAIN (COSTS OFF)
WITH top_customers AS (
  SELECT customer_id, SUM(total) AS revenue
  FROM orders GROUP BY customer_id
  ORDER BY revenue DESC LIMIT 10
)
SELECT c.name, t.revenue
FROM top_customers t JOIN customers c ON c.id = t.customer_id;

EXPLAIN (COSTS OFF)
SELECT c.name, t.revenue
FROM (SELECT customer_id, SUM(total) AS revenue
      FROM orders GROUP BY customer_id
      ORDER BY revenue DESC LIMIT 10) t
JOIN customers c ON c.id = t.customer_id;
