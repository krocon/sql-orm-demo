-- Folge 010 – Schätzung vs. Realität: veraltete Statistiken erzeugen, dann ANALYZE
SET client_min_messages = warning;
CREATE SCHEMA IF NOT EXISTS ep010;
SET search_path = ep010, public;
DROP TABLE IF EXISTS stats_demo;

-- Autovacuum aus, sonst aktualisiert der Hintergrundprozess die Statistik womöglich selbst
CREATE TABLE stats_demo (id bigint, status text) WITH (autovacuum_enabled = off);

-- Zustand beim letzten ANALYZE: 1.000 Zeilen, alle 'NEW'
INSERT INTO stats_demo SELECT g, 'NEW' FROM generate_series(1, 1000) g;
ANALYZE stats_demo;

-- Danach kommen 500.000 Zeilen dazu – alle 'PAID'. Kein ANALYZE!
INSERT INTO stats_demo SELECT g, 'PAID' FROM generate_series(1001, 501000) g;

-- ✘ Statistik sagt „100 % NEW“ → Schätzung ~500.000 Zeilen, tatsächlich 1.000
EXPLAIN (ANALYZE, COSTS ON, TIMING OFF, SUMMARY OFF, BUFFERS OFF)
SELECT * FROM stats_demo WHERE status = 'NEW';

-- ✘ … und für 'PAID' (laut Statistik nicht vorhanden): Schätzung winzig, tatsächlich 500.000
EXPLAIN (ANALYZE, COSTS ON, TIMING OFF, SUMMARY OFF, BUFFERS OFF)
SELECT * FROM stats_demo WHERE status = 'PAID';

-- ✔ Statistik aktualisieren
ANALYZE stats_demo;

EXPLAIN (ANALYZE, COSTS ON, TIMING OFF, SUMMARY OFF, BUFFERS OFF)
SELECT * FROM stats_demo WHERE status = 'NEW';

EXPLAIN (ANALYZE, COSTS ON, TIMING OFF, SUMMARY OFF, BUFFERS OFF)
SELECT * FROM stats_demo WHERE status = 'PAID';

-- Wann wurde zuletzt analysiert?
SELECT relname, n_live_tup, n_mod_since_analyze, last_analyze, last_autoanalyze
FROM pg_stat_user_tables WHERE relname = 'stats_demo';
