-- Folge 028: Produkte mit selbst vergebener ID (kein @GeneratedValue)
CREATE TABLE ep028_products (
  id   bigint PRIMARY KEY,
  name text NOT NULL
);
