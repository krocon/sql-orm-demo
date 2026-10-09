# Folge 039 – Testen mit echter Datenbank            (EN: Testing with a real database)
Video: „Testen mit echter Datenbank in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
H2 ist nicht PostgreSQL – auch im Kompatibilitätsmodus fehlen Datentypen (jsonb), Partial Indexes, `ON CONFLICT … DO UPDATE`, und Sperr-/Transaktionsverhalten ist anders. Testcontainers startet ein echtes `postgres:18`, `@ServiceConnection` verdrahtet die DataSource, `@AutoConfigureTestDatabase(replace = NONE)` verhindert, dass `@DataJpaTest` eine eingebettete DB unterschiebt. Flyway läuft im Test mit.

## Ausprobieren
```bash
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep039.**'      # braucht Docker
# ohne Docker: OrderRepositoryTest wird übersprungen, H2VsPostgresTest läuft gegen DB_URL:
docker compose up -d && set -a && . ./.env && set +a
cd app && DB_URL=jdbc:postgresql://localhost:5433/shop_app ./mvnw test -Dtest='dev.krocon.shop.ep039.**'
```
Container zwischen Läufen wiederverwenden (nur lokal): `episodes/039/testcontainers.properties` nach `~/.testcontainers.properties` kopieren und im Code `new PostgreSQLContainer("postgres:18").withReuse(true)` setzen.

## Dateien
- `app/src/test/java/dev/krocon/shop/ep039/OrderRepositoryTest.java` – genau wie im Video: `@DataJpaTest` + `replace = NONE` + `@Container @ServiceConnection static PostgreSQLContainer` (`org.testcontainers.postgresql.PostgreSQLContainer`, Testcontainers 2 – ohne Generics); `@Testcontainers(disabledWithoutDocker = true)`
- `app/src/test/java/dev/krocon/shop/TestcontainersConfiguration.java` – gemeinsamer Container als `@Bean @ServiceConnection` für alle anderen Tests (ein Container pro Test-Kontext statt pro Klasse; bei gesetzter `DB_URL` kein Container)
- `app/src/test/java/dev/krocon/shop/ShopJpaTest.java` – Meta-Annotation `@DataJpaTest` + `replace = NONE` + Import der Konfiguration
- `app/src/main/resources/db/migration/V380__ep039_order_events.sql` – jsonb-Spalte, Partial Index, GIN-Index, Upsert-Tabelle
- `app/src/test/java/dev/krocon/shop/ep039/H2VsPostgresTest.java` – jsonb (`->>`, `@>`), Partial Index, `ON CONFLICT … DO UPDATE`, transaktionales DDL
- `episodes/039/testcontainers.properties` – Reuse-Schalter für `~/.testcontainers.properties`

## Was du sehen solltest
- Mit Docker: `OrderRepositoryTest` 2/2 grün (5000 Bestellungen, 1000 Kunden – Flyway V1/V2 sind im Container gelaufen). Ohne Docker: 2 Tests *skipped*.
- `H2VsPostgresTest` 4/4 grün gegen PostgreSQL 18: `payload ->> 'carrier'` mit `payload @> '{"type": "SHIPPED"}'` → `DHL`; Index-Definition enthält `WHERE (NOT processed)`; zwei Upserts ergeben `15.50`; `CREATE TABLE` im Test wird mit dem Test-Rollback zurückgenommen (DDL ist in PostgreSQL transaktional).
- Auf H2 (`jdbc:h2:mem:test;MODE=PostgreSQL`) scheitert schon Migration V380: H2 kennt `jsonb` und die Operatoren `->>`/`@>` nicht, keine Partial Indexes (`CREATE INDEX … WHERE`) und kein `USING gin`; der PostgreSQL-Modus unterstützt laut H2-Doku nur `ON CONFLICT DO NOTHING`. H2 ist deshalb bewusst **keine** Dependency im Projekt.
