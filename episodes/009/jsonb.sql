-- Folge 009 – das Dokument aus order.json in PostgreSQL als JSONB speichern und abfragen
-- („Die Grenzen verschwimmen: PostgreSQL kann JSON“)
SET client_min_messages = warning;
DROP SCHEMA IF EXISTS ep009 CASCADE;
CREATE SCHEMA ep009;
SET search_path = ep009, public;

CREATE TABLE order_docs (
  id  bigint PRIMARY KEY,
  doc jsonb NOT NULL
);

-- 1001 = Inhalt von episodes/009/order.json, dazu drei weitere Dokumente
INSERT INTO order_docs (id, doc) VALUES
  (1001, '{ "_id": 1001, "customer": "ana@shop.de",
            "items": [ { "sku": "MUG-1", "qty": 2 } ] }'),
  (1002, '{ "_id": 1002, "customer": "ben@shop.de",
            "items": [ { "sku": "SKU-002", "qty": 1 }, { "sku": "MUG-1", "qty": 1 } ],
            "gift": true }'),                         -- flexible Struktur: neues Feld, kein ALTER TABLE
  (1003, '{ "_id": 1003, "customer": "ana@shop.de",
            "items": [ { "sku": "SKU-010", "qty": 3 } ] }'),
  (1004, '{ "_id": 1004, "customer": "cem@shop.de",
            "items": [ { "sku": "MUG1", "qty": 1 } ] }');   -- Tippfehler in der SKU – niemand prüft

-- Feld lesen: ->> liefert text
SELECT id, doc ->> 'customer' AS customer, jsonb_array_length(doc -> 'items') AS positionen
FROM order_docs ORDER BY id;

-- Containment @>: alle Bestellungen, die MUG-1 enthalten → 1001, 1002
SELECT id FROM order_docs
WHERE doc @> '{"items": [{"sku": "MUG-1"}]}'
ORDER BY id;

-- Array „auspacken“ = wieder relational: eine Zeile pro Position
SELECT d.id, item ->> 'sku' AS sku, (item ->> 'qty')::int AS qty
FROM order_docs d, jsonb_array_elements(d.doc -> 'items') AS item
ORDER BY d.id, sku;

-- Stück pro SKU über alle Dokumente – mit JOIN auf die relationale Produkttabelle
SELECT item ->> 'sku' AS sku, p.name, sum((item ->> 'qty')::int) AS stueck
FROM order_docs d
CROSS JOIN jsonb_array_elements(d.doc -> 'items') AS item
LEFT JOIN public.products p ON p.sku = item ->> 'sku'
GROUP BY 1, 2
ORDER BY 1;

-- Index für @>-Abfragen: GIN
CREATE INDEX order_docs_doc_gin ON order_docs USING gin (doc jsonb_path_ops);

-- Grenze des Dokumentmodells: nichts hindert ein Dokument an einer unbekannten SKU
-- (Dokument 1004: „MUG1“) – relational verhindert das ein Fremdschlüssel.
SELECT d.id, item ->> 'sku' AS unbekannte_sku
FROM order_docs d, jsonb_array_elements(d.doc -> 'items') AS item
WHERE NOT EXISTS (SELECT 1 FROM public.products p WHERE p.sku = item ->> 'sku');
