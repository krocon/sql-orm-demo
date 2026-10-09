# sql-orm-demo

Code und Demos zur Videoserie **„SQL, ORM & Hibernate in 60 Sekunden“** (DE + EN).
Zu jeder Folge gibt es einen Ordner `episodes/NNN/` mit README (Befehle, erwartete Ausgabe) und dem Code aus dem Video – vollständig und lauffähig.

*EN: Code for the video series “SQL, ORM & Hibernate in 60 seconds”. Every episode has its own folder `episodes/NNN/` with a README and runnable code.*

## Voraussetzungen

- Docker (für PostgreSQL 18 per Compose und für Testcontainers)
- `psql` (optional – die Skripte nutzen sonst `docker compose exec`)
- Java 17+ für die App ab Folge 011 (Maven Wrapper liegt bei)

## Schnellstart

```bash
git clone https://github.com/krocon/sql-orm-demo.git && cd sql-orm-demo
cp .env.example .env              # Passwort setzen
docker compose up -d              # PostgreSQL 18: Datenbanken shop + shop_app

# Block 01: alle SQL-Folgen gegen die Datenbank „shop“ ausführen
./scripts/run-all.sh

# Block 02–04: alle Java-Tests (Testcontainers startet postgres:18 selbst)
cd app && ./mvnw test
# eine Folge:  ./mvnw test -Dtest='dev.krocon.shop.ep017.**'
# ohne Testcontainers gegen die Compose-DB:
#   DB_URL=jdbc:postgresql://localhost:5433/shop_app POSTGRES_USER=shop POSTGRES_PASSWORD=… ./mvnw test
```

## Aufbau

```
compose.yaml            postgres:18, Port 5433 nur auf 127.0.0.1, Volume, Healthcheck
docker/initdb/          legt beim ersten Start zusätzlich die Datenbank shop_app an
schema/shop.sql         Beispiel-Schema „Online-Shop“ + Testdaten (wiederholbar)
scripts/run-all.sh      schema/shop.sql + alle SQL-Dateien aus episodes/001–010
episodes/NNN/           README + Code pro Folge
app/                    Spring Boot 4.1 · Hibernate 7.4 · Spring Data JPA · Flyway · Testcontainers 2
  src/main/java/dev/krocon/shop/domain/   Customer, Order, OrderItem, Product (gemeinsam für alle Folgen)
  src/main/java/dev/krocon/shop/epNNN/    Code einer Folge (eigene Entities heißen EpNNN…)
  src/test/java/dev/krocon/shop/epNNN/    Tests, die Problem und Lösung der Folge beweisen
  src/main/resources/db/migration/        Flyway: V1/V2 Shop, V1xx–V3xx Tabellen einzelner Folgen
```

## Das Beispiel-Schema

Alle 40 Folgen nutzen denselben kleinen Shop: `customers` (1000, darunter ana@shop.de ohne Bestellung), `orders` (5000), `order_items` (10 000), `products` (50, u. a. MUG-1), `categories` (Baum über parent_id). Die Daten sind deterministisch – Zahlen in den READMEs sind reproduzierbar.

## Folgen

### Block 01 – SQL & Datenbanken
reines SQL: `episodes/NNN/*.sql`

| Folge | Thema | Englisch |
|---|---|---|
| [001](episodes/001/) | Warum SQL auch heute unverzichtbar ist | Why SQL still matters |
| [002](episodes/002/) | SQL-Basics: SELECT, WHERE, ORDER BY | SQL basics: SELECT, WHERE, ORDER BY |
| [003](episodes/003/) | JOINs: INNER, LEFT und RIGHT | JOINs explained: INNER, LEFT, RIGHT |
| [004](episodes/004/) | GROUP BY & Aggregationen | GROUP BY & aggregations explained |
| [005](episodes/005/) | Subqueries vs. CTEs | Subqueries vs. CTEs explained |
| [006](episodes/006/) | Indexes: Warum Datenbanken langsam werden | Indexes: why databases get slow |
| [007](episodes/007/) | Transaktionen & ACID | Transactions & ACID explained |
| [008](episodes/008/) | Normalisierung: Redundanzen vermeiden | Database normalization: avoid redundancy |
| [009](episodes/009/) | SQL vs. NoSQL: Wann passt was? | SQL vs. NoSQL: which fits when? |
| [010](episodes/010/) | Query-Optimierung mit EXPLAIN | Query optimization with EXPLAIN |

### Block 02 – Object-Relational Mapping
Spring Boot / JPA: `app/…/epNNN/`

| Folge | Thema | Englisch |
|---|---|---|
| [011](episodes/011/) | Was ist ORM? Objekte statt Zeilen | What is ORM? Objects, not rows |
| [012](episodes/012/) | Der Impedance Mismatch | The object-relational impedance mismatch |
| [013](episodes/013/) | ORM vs. Native SQL | ORM vs. native SQL |
| [014](episodes/014/) | Entity Lifecycle: New, Managed, Detached, Removed | Entity lifecycle: new, managed, detached, removed |
| [015](episodes/015/) | Beziehungen: 1:1, 1:n, n:m | Entity relationships: one-to-one, one-to-many, many-to-many |
| [016](episodes/016/) | Lazy vs. Eager Loading | Lazy vs. eager loading |
| [017](episodes/017/) | Das N+1-Problem | The N+1 query problem |
| [018](episodes/018/) | First- & Second-Level-Cache | First- & second-level cache |

### Block 03 – Hibernate & JPA
Spring Boot / JPA: `app/…/epNNN/`

| Folge | Thema | Englisch |
|---|---|---|
| [019](episodes/019/) | JPA vs. Hibernate | JPA vs. Hibernate |
| [020](episodes/020/) | Entities & Primary Keys | Entities & primary keys |
| [021](episodes/021/) | Entity Mapping: @Column, @Table, Constraints | Entity mapping: @Column, @Table, constraints |
| [022](episodes/022/) | @OneToMany & Co. | Relationship mapping: @OneToMany & co. |
| [023](episodes/023/) | Cascade & Orphan Removal | Cascade & orphan removal |
| [024](episodes/024/) | Der Persistence Context | The persistence context |
| [025](episodes/025/) | Dirty Checking: Updates ohne save() | Dirty checking: updates without save() |
| [026](episodes/026/) | Flush vs. Commit | Flush vs. commit |
| [027](episodes/027/) | JPQL vs. Native SQL | JPQL vs. native SQL |
| [028](episodes/028/) | Spring Data JPA | Spring Data JPA |

### Block 04 – Best Practices
Spring Boot / JPA: `app/…/epNNN/`

| Folge | Thema | Englisch |
|---|---|---|
| [029](episodes/029/) | DTOs vs. Entities | DTOs vs. entities |
| [030](episodes/030/) | Optimistic vs. Pessimistic Locking | Optimistic vs. pessimistic locking |
| [031](episodes/031/) | Migrationen: Flyway & Liquibase | Database migrations: Flyway & Liquibase |
| [032](episodes/032/) | Bulk Updates & Deletes | Bulk updates & deletes |
| [033](episodes/033/) | Pagination & Sorting | Pagination & sorting |
| [034](episodes/034/) | Hibernate Performance Tuning | Hibernate performance tuning |
| [035](episodes/035/) | Open Session in View | Open Session in View |
| [036](episodes/036/) | Auditing & Soft Deletes | Auditing & soft deletes |
| [037](episodes/037/) | Schema Generation vs. Migrationen | Schema generation vs. migrations |
| [038](episodes/038/) | ORM-Anti-Patterns | Common ORM anti-patterns |
| [039](episodes/039/) | Testen mit echter Datenbank | Testing with a real database |
| [040](episodes/040/) | Alternativen: jOOQ, MyBatis, JDBC | ORM alternatives: jOOQ, MyBatis and JDBC compared |

## Stand

Getestet im Oktober 2026 mit PostgreSQL 18.4, Spring Boot 4.1.0, Hibernate ORM 7.4.1, Testcontainers 2.0.5, Java 17 (Bytecode-Ziel; gebaut und getestet mit JDK 21).
Gemessene Zeiten in den READMEs sind gerundet und hängen von der Maschine ab.
