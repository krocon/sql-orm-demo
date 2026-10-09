-- Folge 005 – WITH RECURSIVE: Kategorienbaum über categories(parent_id) durchlaufen
WITH RECURSIVE baum AS (
  -- Startpunkt: Wurzeln (ohne Elternkategorie)
  SELECT id, parent_id, name, 1 AS ebene, name AS pfad
  FROM categories
  WHERE parent_id IS NULL
  UNION ALL
  -- Rekursionsschritt: Kinder der bisher gefundenen Kategorien
  SELECT k.id, k.parent_id, k.name, b.ebene + 1, b.pfad || ' > ' || k.name
  FROM categories k
  JOIN baum b ON k.parent_id = b.id
)
SELECT id, ebene, repeat('  ', ebene - 1) || name AS kategorie, pfad
FROM baum
ORDER BY pfad;

-- Alle Unterkategorien von „Kleidung“ (beliebig tief) und ihre Produktanzahl
WITH RECURSIVE unter AS (
  SELECT id FROM categories WHERE name = 'Kleidung'
  UNION ALL
  SELECT k.id FROM categories k JOIN unter u ON k.parent_id = u.id
)
SELECT count(*) AS produkte_in_kleidung
FROM products
WHERE category_id IN (SELECT id FROM unter);
