-- Contract: erst wenn KEIN laufender Code mehr "status" nutzt (nächstes Release)
ALTER TABLE orders DROP COLUMN status;
