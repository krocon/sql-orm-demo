# Folge 017 – Das N+1-Problem   (EN: The N+1 query problem)
Video: „Das N+1-Problem in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Eine Query lädt die Bestellungen, dann holt jeder Zugriff auf den Lazy-Kunden in der Schleife eine eigene
Query: aus 1 werden N+1. Die Tests zählen die Queries über die Hibernate-Statistik
(`Statistics#getPrepareStatementCount`) und zeigen vier Lösungen: Entity Graph, JOIN FETCH, Batch Fetching, DTO-Projektion.

## Ausprobieren
```bash
cd app
./mvnw test -Dtest='dev.krocon.shop.ep017.**'     # PostgreSQL 18 per Testcontainers (Docker nötig)

# alternativ gegen die Compose-Datenbank (Flyway legt Schema + Testdaten in shop_app an):
docker compose up -d                             # im Repo-Wurzelverzeichnis, .env aus .env.example
DB_URL=jdbc:postgresql://127.0.0.1:5433/shop_app POSTGRES_USER=shop POSTGRES_PASSWORD=change-me \
  ./mvnw test -Dtest='dev.krocon.shop.ep017.**'
```

## Dateien
- `app/src/main/java/dev/krocon/shop/ep017/OrderListService.java` – Schleife mit `log.info(o.getCustomer().getName())` (Problem) und dieselbe Schleife mit Entity Graph.
- `app/src/main/java/dev/krocon/shop/ep017/Ep017OrderRepository.java` – `findByStatus` (Problem), `@EntityGraph findWithCustomerByStatus`, JOIN FETCH, DTO-Query.
- `app/src/main/java/dev/krocon/shop/ep017/OrderRow.java` – `record OrderRow(Long id, String customerName)`.
- `app/src/test/java/dev/krocon/shop/ep017/NPlusOneTest.java` – Problem und Lösungen mit Assertions auf die Query-Anzahl.
- `app/src/test/java/dev/krocon/shop/ep017/BatchFetchTest.java` – derselbe Problem-Code mit `hibernate.default_batch_fetch_size=50` (per `@TestPropertySource`; global ginge es über `spring.jpa.properties.hibernate.default_batch_fetch_size: 50` in `application.yaml`).
- `generate_statistics: true` steht bereits in `app/src/main/resources/application.yaml`.

## Was du sehen solltest
Testdaten: 1.000 Bestellungen mit Status `NEW`, von 999 verschiedenen Kunden.

| Variante | Bestellungen | Queries |
|---|---|---|
| Problem: `findByStatus(NEW, Limit.of(100))` + Schleife | 100 | **101** |
| Problem: `findByStatus(NEW)` + Schleife | 1.000 | **1.000** (1 + 999) |
| `@EntityGraph(attributePaths = "customer")` | 100 | 1 |
| `JOIN FETCH` | 1.000 | 1 |
| `default_batch_fetch_size = 50` | 100 / 1.000 | 3 / 21 |
| DTO `OrderRow` | 1.000 | 1 (0 Entities geladen) |

Batch Fetching lädt die Kunden auf PostgreSQL mit einem Array-Parameter statt IN-Liste:
`select … from customers c1_0 where c1_0.id = any (?)` – eine Query pro 50 Kunden.
