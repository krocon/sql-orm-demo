# Folge 027 – JPQL vs. Native SQL            (EN: JPQL vs. native SQL)
Video: „JPQL vs. Native SQL in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
JPQL arbeitet auf Entities (`from Order`, nicht `from orders`) und bindet Parameter sicher. Native SQL nimmst du für Datenbank-Features wie JSONB-Operatoren; Window Functions und CTEs kann auch Hibernates HQL (seit 6), Standard-JPQL nicht. Criteria API für dynamische Filter, DTO-Projektion per `select new` mit Record.

## Ausprobieren
```bash
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep027.**'
```

## Dateien
- `app/src/main/java/dev/krocon/shop/ep027/OrderQueries.java` – JPQL + `select new`, unsichere/sichere Native Query, JSONB `@>`, Window Function nativ und als HQL mit CTE, Criteria API
- `app/src/main/java/dev/krocon/shop/ep027/OrderSummary.java`, `OrderRank.java`, `OrderFilter.java` – Records
- `app/src/main/resources/db/migration/V270__ep027_order_attrs.sql` – `ep027_order_attrs(attrs jsonb)` für Bestellungen 1–300
- `app/src/test/java/dev/krocon/shop/ep027/OrderQueriesTest.java` – DTO-Projektion, JSONB, Native = HQL, Criteria
- `app/src/test/java/dev/krocon/shop/ep027/InjectionTest.java` – String-Verkettung vs. gebundener Parameter
- `app/src/test/java/dev/krocon/shop/ep027/JpqlComplianceTest.java` – mit `hibernate.jpa.compliance.query=true` lehnt Hibernate Window Function und CTE ab

## Was du sehen solltest
```
PAID-Bestellungen als OrderSummary: 2000, z. B. OrderSummary[id=7, email=kunde489@shop.example, total=392.15]
JSONB @> {channel: app, gift: true}: 10 Bestellungen, z. B. [30, 60, 90]
Top je Kunde: OrderRank[orderId=138, customerId=916, total=491.29, rank=1]
Verkettet mit "PAID' OR '1'='1": 5000 Bestellungen
Strict JPQL: … StrictJpaComplianceViolation: … use of CTEs (common table expressions)
Strict JPQL: … non-compliant non-standard function call [rank] …
```
- Gebundener Parameter mit demselben Angriffstext → 0 Treffer.
- HQL-CTE wird zu `with ranked (…) as (select …, rank() over(partition by o1_0.customer_id order by o1_0.total desc) from orders o1_0) select … fetch first ? rows only` – Ergebnis identisch zur Native Query.
- Criteria: ohne Filter 5000, `PAID` ab 400 € 67, `kunde1@shop.example` 5 Bestellungen.
