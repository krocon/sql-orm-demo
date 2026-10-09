-- Folge 015 (Problem): @ManyToMany Order–Product. Die Join-Tabelle hat nur zwei IDs,
-- für quantity und unit_price ist kein Platz (Lösung: Entity OrderItem / order_items).
CREATE TABLE ep015_order_products (
  order_id   bigint NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
  product_id bigint NOT NULL REFERENCES products (id)
);
