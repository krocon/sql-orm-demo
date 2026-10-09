-- Folge 039: PostgreSQL-spezifisch – läuft so nicht auf H2 (auch nicht im PostgreSQL-Modus)
CREATE TABLE ep039_order_events (
  id        bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id  bigint NOT NULL REFERENCES orders (id),
  payload   jsonb NOT NULL,                  -- jsonb: kein H2-Datentyp
  processed boolean NOT NULL DEFAULT false
);
-- Partial Index: nur offene Events
CREATE INDEX ep039_order_events_open_idx ON ep039_order_events (order_id) WHERE NOT processed;
-- GIN-Index für jsonb-Abfragen mit @>
CREATE INDEX ep039_order_events_payload_idx ON ep039_order_events USING gin (payload);

-- für das Upsert-Beispiel (ON CONFLICT … DO UPDATE)
CREATE TABLE ep039_daily_revenue (
  day     date PRIMARY KEY,
  revenue numeric(12,2) NOT NULL
);
