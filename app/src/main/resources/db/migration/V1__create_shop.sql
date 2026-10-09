-- Shop-Schema (gleich wie schema/shop.sql, plus products.version für @Version)
CREATE TABLE customers (
  id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  email      text NOT NULL UNIQUE,
  name       text NOT NULL,
  country    text NOT NULL,
  city       text,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE categories (
  id        bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  parent_id bigint REFERENCES categories (id),
  name      text NOT NULL
);

CREATE TABLE products (
  id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  sku         text NOT NULL UNIQUE,
  name        text NOT NULL,
  category_id bigint REFERENCES categories (id),
  price       numeric(10,2) NOT NULL CHECK (price >= 0),
  stock       integer NOT NULL CHECK (stock >= 0),
  version     bigint NOT NULL DEFAULT 0
);

CREATE TABLE orders (
  id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  customer_id bigint NOT NULL REFERENCES customers (id),
  status      text NOT NULL DEFAULT 'NEW'
              CHECK (status IN ('NEW', 'PAID', 'SHIPPED', 'CANCELLED')),
  total       numeric(10,2) NOT NULL DEFAULT 0,
  created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE order_items (
  id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id   bigint NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
  product_id bigint NOT NULL REFERENCES products (id),
  quantity   integer NOT NULL CHECK (quantity > 0),
  unit_price numeric(10,2) NOT NULL   -- Preis zum Bestellzeitpunkt (Historie, keine Redundanz)
);
