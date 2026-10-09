-- Expand 2/2: Daten kopieren. Danach schreibt der Code beide Spalten und liest "state".
UPDATE orders SET state = status WHERE state IS NULL;
ALTER TABLE orders ALTER COLUMN state SET DEFAULT 'NEW';
ALTER TABLE orders ALTER COLUMN state SET NOT NULL;
