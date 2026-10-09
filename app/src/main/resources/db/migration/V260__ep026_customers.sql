-- Folge 026: Kunde mit SEQUENCE-ID → INSERT (und UNIQUE-Fehler) erst beim Flush
CREATE SEQUENCE ep026_customers_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE ep026_customers (
  id    bigint PRIMARY KEY,
  email text NOT NULL UNIQUE,
  name  text NOT NULL
);
