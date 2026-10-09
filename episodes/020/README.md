# Folge 020 – Entities & Primary Keys            (EN: Entities & primary keys)
Video: „Entities & Primary Keys in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Eine Entity braucht `@Entity`, `@Id` und einen (gern `protected`) no-arg-Konstruktor. Bei `SEQUENCE` kennt Hibernate die ID vor dem INSERT (50 IDs pro Sequenzaufruf) und kann INSERTs per JDBC bündeln; bei `IDENTITY` muss jedes INSERT sofort einzeln laufen. `GenerationType.UUID` erzeugt die ID in Java; `equals/hashCode` müssen mit `id == null` klarkommen.

## Ausprobieren
```bash
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep020.**'
# Batches im Log sehen:
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep020.BatchInsertTest#sequenceBatchesInserts' \
  -Dlogging.level.org.hibernate.orm.jdbc.batch=TRACE
```
Der Test setzt `hibernate.jdbc.batch_size=50` per `@TestPropertySource` (in `application.yaml` ist es nicht gesetzt).

## Dateien
- `app/src/main/java/dev/krocon/shop/ep020/Ep020SequenceOrder.java` – `SEQUENCE` mit `@SequenceGenerator(allocationSize = 50)`, null-sicheres `equals/hashCode`
- `app/src/main/java/dev/krocon/shop/ep020/Ep020IdentityOrder.java` – `IDENTITY`-Variante zum Vergleich
- `app/src/main/java/dev/krocon/shop/ep020/Ep020UuidOrder.java` – `GenerationType.UUID`
- `app/src/main/resources/db/migration/V200__ep020_ids.sql` – Tabellen `ep020_*` und Sequenz `INCREMENT BY 50`
- `app/src/test/java/dev/krocon/shop/ep020/BatchInsertTest.java` – INSERT-Zeitpunkt, 1000-Zeilen-Import, UUID, equals/hashCode

## Was du sehen solltest
```
IDENTITY: 1000 Zeilen → 1000 JDBC-Statements
SEQUENCE: 1000 Zeilen → 21 JDBC-Statements (Sequenz-Aufrufe + INSERT-Batches à 50)
```
- IDENTITY: `persist()` schickt sofort `insert into ep020_identity_orders …` (ID ist danach gesetzt), trotz `batch_size=50`.
- SEQUENCE: `persist()` holt nur eine ID (`select nextval('ep020_sequence_orders_seq')`, 1× pro 50 Entities); die INSERTs kommen erst beim `flush()` – im TRACE-Log 20× `Executing JDBC batch (50 / 50)`.
- UUID: ID ist nach `persist()` gesetzt, ohne ein einziges SQL-Statement.
- Ein `HashSet` findet die Entity auch nach der ID-Vergabe wieder (konstanter `hashCode`).
