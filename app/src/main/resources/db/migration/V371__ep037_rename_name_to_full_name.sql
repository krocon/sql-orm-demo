-- Folge 037: Feld name → fullName. Die Migration benennt um – die Daten bleiben erhalten.
-- (ddl-auto: update würde stattdessen eine leere Spalte full_name anlegen.)
ALTER TABLE ep037_customers RENAME COLUMN name TO full_name;
