-- Folge 002 – Lesereihenfolge: FROM → WHERE → SELECT → ORDER BY
-- Ein Spaltenalias aus SELECT existiert in ORDER BY, aber noch nicht in WHERE.

-- ✔ Alias in ORDER BY: funktioniert
SELECT name, created_at::date AS registriert
FROM customers
WHERE country = 'DE'
ORDER BY registriert DESC
LIMIT 5;

-- ✔ Lösung für WHERE: Ausdruck wiederholen (oder Unterabfrage/CTE)
SELECT name, created_at::date AS registriert
FROM customers
WHERE created_at::date >= DATE '2026-08-01'
ORDER BY registriert DESC;
