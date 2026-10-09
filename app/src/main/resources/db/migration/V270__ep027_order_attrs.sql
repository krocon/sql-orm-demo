-- Folge 027: JSONB-Attribute zu Bestellungen (nur per Native SQL abgefragt, keine Entity)
CREATE TABLE ep027_order_attrs (
  order_id bigint PRIMARY KEY REFERENCES orders (id) ON DELETE CASCADE,   -- stört keine anderen Folgen
  attrs    jsonb NOT NULL
);
INSERT INTO ep027_order_attrs (order_id, attrs)
SELECT id,
       jsonb_build_object('channel', CASE WHEN id % 3 = 0 THEN 'app' ELSE 'web' END,
                          'gift', id % 10 = 0)
FROM orders
WHERE id <= 300;
