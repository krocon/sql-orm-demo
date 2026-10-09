-- Expand 1/2: neue Spalte "state" neben "status" (nullable, alter Code läuft weiter)
ALTER TABLE orders ADD COLUMN state varchar(20);
