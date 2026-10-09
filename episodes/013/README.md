# Folge 013 – ORM vs. Native SQL   (EN: ORM vs. native SQL)
Video: „ORM vs. Native SQL in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Umsatz pro Land über Entities heißt: alle Bestellungen laden, für jeden Kunden eine Extra-Query, in Java
summieren. Dieselbe Frage als SQL mit `GROUP BY ROLLUP` erledigt die Datenbank in einer Abfrage – per
`JdbcClient` direkt neben JPA im selben Projekt.

## Ausprobieren
```bash
cd app
./mvnw test -Dtest='dev.krocon.shop.ep013.**'     # PostgreSQL 18 per Testcontainers (Docker nötig)

# alternativ gegen die Compose-Datenbank (Flyway legt Schema + Testdaten in shop_app an):
docker compose up -d                             # im Repo-Wurzelverzeichnis, .env aus .env.example
DB_URL=jdbc:postgresql://127.0.0.1:5433/shop_app POSTGRES_USER=shop POSTGRES_PASSWORD=change-me \
  ./mvnw test -Dtest='dev.krocon.shop.ep013.**'
```
Nur SQL: `psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/013/revenue.sql`

## Dateien
- `app/src/main/java/dev/krocon/shop/ep013/RevenueService.java` – `revenueWithEntities()` (Problem-Schleife aus dem Video) und `revenueWithSql()` (JdbcClient + ROLLUP).
- `app/src/main/java/dev/krocon/shop/ep013/Revenue.java` – Record `(String country, BigDecimal total)`.
- `app/src/test/java/dev/krocon/shop/ep013/RevenueServiceTest.java` – zählt Queries und geladene Entities beider Varianten, vergleicht die Summen.
- `episodes/013/revenue.sql` – die Report-Abfrage pur für psql.

## Was du sehen solltest
- Entity-Schleife: **1.000 Queries** (1 × orders + 999 × customers) und **5.999 Entities** im Speicher.
- SQL: 1 Abfrage, 0 Entities (läuft an Hibernate vorbei, daher 0 in der Hibernate-Statistik), gleiche Zahlen:

  | country | total |
  |---|---|
  | AT | 148.675,39 |
  | CH | 147.400,71 |
  | DE | 442.121,08 |
  | FR | 145.722,47 |
  | NL | 147.478,47 |
  | *(NULL = Summe)* | 1.031.398,12 |
