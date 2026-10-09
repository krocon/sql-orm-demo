-- Folge 002 – Abfrage aus dem Video (Code-Fenster „Hook“)
-- Nur benötigte Spalten, Zeilen filtern, sortieren, begrenzen.
SELECT name, email, created_at
FROM customers
WHERE country = 'DE'
  AND created_at >= DATE '2026-01-01'
ORDER BY created_at DESC
LIMIT 20;
