# Folge 034 – Hibernate Performance Tuning            (EN: Hibernate Performance Tuning)
Video: „Hibernate Performance Tuning in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Erst messen, dann tunen: SQL-Log, Bind-Parameter und Statistics machen sichtbar, was Hibernate tut. Ein Assert auf die Statement-Anzahl im Test fängt N+1, bevor es live geht. Dann den Fetch-Plan reparieren (JOIN FETCH, Entity Graph, Batch Size, Projektion) und die Datenbank mit EXPLAIN ANALYZE fragen.

## Ausprobieren
```bash
cd app
./mvnw test -Dtest='dev.krocon.shop.ep034.**'
# EXPLAIN vor/nach Index (Repo-Wurzel, Compose-DB):
docker compose exec -T db psql -U shop -d shop < episodes/034/explain.sql
```
Logging aus dem Video als Profil: `app/src/test/resources/application-ep034.yaml` (in eigenen Tests per `@ActiveProfiles("ep034")`).

## Dateien
- `app/src/test/resources/application-ep034.yaml` – `org.hibernate.SQL: debug`, `org.hibernate.orm.jdbc.bind: trace`, `org.hibernate.session.metrics: debug`, `generate_statistics: true`
- `app/src/test/java/dev/krocon/shop/ep034/QueryCount.java` – **wiederverwendbarer Test-Helfer**: `QueryCount.of(em).assertStatements(1, () -> …)` (Hibernate Statistics)
- `app/src/test/java/dev/krocon/shop/ep034/SqlRecorder.java` – StatementInspector, der jedes SQL aufzeichnet (`@TestPropertySource(properties = SqlRecorder.PROPERTY)`)
- `app/src/main/java/dev/krocon/shop/ep034/OrderService.java` – Bestellseite: naiv (N+1), JOIN FETCH, Entity Graph, Projektion; `@Transactional(readOnly = true)`
- `app/src/main/java/dev/krocon/shop/ep034/Ep034OrderRepository.java`, `OrderSummary.java` – Queries und Projektions-Record
- `app/src/test/java/dev/krocon/shop/ep034/SqlLoggingTest.java` – prüft SQL-, Bind- und Session-Metrics-Logausgabe
- `app/src/test/java/dev/krocon/shop/ep034/QueryCountTest.java` – N+1 messen, Assert schlägt fehl; Fixes mit genau 1 Statement
- `app/src/test/java/dev/krocon/shop/ep034/BatchFetchTest.java` – `default_batch_fetch_size=100` als Notbremse
- `episodes/034/explain.sql` – EXPLAIN ANALYZE vor und nach Index auf `orders(customer_id)` (mit ROLLBACK)

## Was du sehen solltest
- Log mit Profil `ep034`:
  `org.hibernate.SQL : select … from orders o1_0 where o1_0.id=?`
  `org.hibernate.orm.jdbc.bind : binding parameter (1:BIGINT) <- [42]`
  `org.hibernate.session.metrics : HHH000401: Logging session metrics: … executing 1 JDBC statements …`
- Hibernate 7.4 loggt die Session Metrics nur mit `org.hibernate.session.metrics: debug` – `generate_statistics` allein reicht nicht.
- N+1: `findAll()` + `getCustomer().getName()` = **1000 Statements** (1 + 999 Kunden); `assertStatements(1, …)` wird rot.
- JOIN FETCH, `@EntityGraph` und Projektion: je **1 Statement**; Projektion lädt 0 Entities.
- `default_batch_fetch_size=100`: derselbe naive Code mit **11 Statements**.
- EXPLAIN (`customer_id = 42`): vorher `Seq Scan on orders`, `Rows Removed by Filter: 4995`; nach `CREATE INDEX` `Bitmap Index Scan on orders_customer_id_idx`, 5 Zeilen.
