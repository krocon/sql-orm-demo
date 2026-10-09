-- Folge 031: Beispiel aus dem Video (eigenes Schema ep031_demo, nicht die App-Tabellen)
CREATE TABLE orders (
  id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  customer_id bigint NOT NULL,
  total       numeric(10,2) NOT NULL DEFAULT 0,
  created_at  timestamptz NOT NULL DEFAULT now()
);

INSERT INTO orders (customer_id, total) VALUES (1, 19.90), (2, 5.00), (3, 42.00);
