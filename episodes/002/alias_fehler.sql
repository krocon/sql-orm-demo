-- Folge 002 – absichtlicher Fehler: Alias in WHERE
-- ON_ERROR_STOP aus, damit scripts/run-all.sh hier nicht abbricht.
\set ON_ERROR_STOP 0

-- ✘ ERROR:  column "registriert" does not exist
SELECT name, created_at::date AS registriert
FROM customers
WHERE registriert >= DATE '2026-08-01';

\set ON_ERROR_STOP 1
