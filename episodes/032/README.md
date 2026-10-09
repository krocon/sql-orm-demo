# Folge 032 – Bulk Updates & Deletes            (EN: Bulk Updates & Deletes)
Video: „Bulk Updates & Deletes in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Alte Bestellungen in einer Schleife laden und per Dirty Checking ändern heißt: N Entities im Speicher und N einzelne UPDATEs. Ein `@Modifying`-JPQL-Update erledigt das mit einem Statement – umgeht aber den Persistence Context, daher `clearAutomatically`/`flushAutomatically`. Für viele Inserts: `hibernate.jdbc.batch_size` + SEQUENCE-IDs, in Chunks mit `flush()`/`clear()`.

## Ausprobieren
```bash
cd app
./mvnw test -Dtest='dev.krocon.shop.ep032.**'
```

## Dateien
- `app/src/main/java/dev/krocon/shop/ep032/Ep032OrderRepository.java` – `findByCreatedAtBefore`, `archiveOlderThan` (`@Modifying(clearAutomatically = true, flushAutomatically = true)`) und Varianten ohne clear/flush
- `app/src/main/java/dev/krocon/shop/ep032/Ep032BatchItem.java` – Entity mit SEQUENCE (`allocationSize = 50`)
- `app/src/main/java/dev/krocon/shop/ep032/Ep032IdentityItem.java` – gleiche Entity mit IDENTITY zum Vergleich
- `app/src/main/resources/db/migration/V310__ep032_batch_items.sql` – Sequence + zwei Tabellen
- `app/src/test/java/dev/krocon/shop/ep032/LoopArchiveTest.java` – Schleife vs. Bulk-UPDATE (Statistics + SQL)
- `app/src/test/java/dev/krocon/shop/ep032/StaleEntityTest.java` – ohne clear veraltete Entity; flush-Verhalten
- `app/src/test/java/dev/krocon/shop/ep032/BatchInsertTest.java` – 10 000 Inserts mit `batch_size=50`, Chunks à 500, Session Metrics

## Was du sehen solltest
- Schleife (Orders vor 01.07.2025): 329 Entities geladen, **263 UPDATEs** (66 waren schon CANCELLED) = 264 Statements.
- Bulk: **1 Statement** `update orders o1_0 set status=? where o1_0.created_at<?`, Rückgabe 329 Zeilen, 0 Entities geladen.
- Ohne `clearAutomatically`: DB sagt `CANCELLED`, die geladene Entity – und sogar `findById` – noch `NEW`. Mit: Kontext geleert, frisch geladen `CANCELLED`.
- Gemessen mit Hibernate 7.4: Auch ohne `flushAutomatically` landet eine offene Änderung vor dem Bulk-UPDATE in der DB (FlushMode AUTO flusht vor JPQL auf dieselbe Tabelle). `flushAutomatically = true` macht das explizit und unabhängig vom FlushMode.
- Batching (Session Metrics): SEQUENCE → **200 JDBC-Batches** à 50 + 200 × `nextval`; IDENTITY → **0 Batches, 10 000 einzelne INSERTs** trotz `batch_size=50`.
- Hinweis: Das Demo-Enum hat kein `ARCHIVED` (Check-Constraint in der DB) – die Tests „archivieren“ mit `CANCELLED`.
