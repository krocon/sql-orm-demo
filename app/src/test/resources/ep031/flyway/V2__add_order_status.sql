ALTER TABLE orders ADD COLUMN status varchar(20);
UPDATE orders SET status = 'NEW' WHERE status IS NULL;
ALTER TABLE orders ALTER COLUMN status SET NOT NULL;
-- Flyway merkt sich V2 in flyway_schema_history
