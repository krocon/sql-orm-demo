-- Läuft nur beim allerersten Start (leeres Volume).
-- shop     → SQL-Folgen 001–010 (schema/shop.sql, scripts/run-all.sh)
-- shop_app → Spring-Boot-App ab Folge 011 (Flyway legt das Schema an)
CREATE DATABASE shop_app;
