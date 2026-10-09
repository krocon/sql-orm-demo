-- Folge 036: Soft-Delete-Falle – ein normaler UNIQUE(email) würde die E-Mail eines
-- gelöschten Kunden für immer blockieren. Lösung: Unique nur für nicht gelöschte Zeilen.
CREATE UNIQUE INDEX ep036_customers_email_active_uq
  ON ep036_customers (email) WHERE NOT deleted;
