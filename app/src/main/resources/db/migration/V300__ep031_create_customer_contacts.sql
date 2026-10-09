-- Folge 031: eigene Demo-Tabelle (die shop-Tabellen bleiben unangetastet)
CREATE TABLE ep031_customer_contacts (
  id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  customer_id bigint NOT NULL REFERENCES customers (id),
  phone       text                -- freies Format, so wie Kunden es eintippen
);

INSERT INTO ep031_customer_contacts (customer_id, phone) VALUES
  (1, '040 1234567'),
  (2, '030/987 654'),
  (3, NULL);
