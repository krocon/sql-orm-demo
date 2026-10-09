# Folge 011 – Was ist ORM? Objekte statt Zeilen   (EN: What is ORM? Objects, not rows)
Video: „Was ist ORM? Objekte statt Zeilen in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Ohne ORM mappst du jedes `ResultSet` von Hand – Spalte für Spalte, Tippfehler fallen erst zur Laufzeit auf,
und der Kunde ist nur eine Zahl. Mit JPA/Hibernate wird die Klasse zur Tabelle, das Feld zur Spalte,
die Referenz zum Fremdschlüssel, und die Unit of Work schreibt Änderungen gesammelt beim Flush/Commit.

## Ausprobieren
```bash
cd app
./mvnw test -Dtest='dev.krocon.shop.ep011.**'     # PostgreSQL 18 per Testcontainers (Docker nötig)

# alternativ gegen die Compose-Datenbank (Flyway legt Schema + Testdaten in shop_app an):
docker compose up -d                             # im Repo-Wurzelverzeichnis, .env aus .env.example
DB_URL=jdbc:postgresql://127.0.0.1:5433/shop_app POSTGRES_USER=shop POSTGRES_PASSWORD=change-me \
  ./mvnw test -Dtest='dev.krocon.shop.ep011.**'
```
Das erzeugte SQL steht im Log (`org.hibernate.SQL: debug` in `app/src/main/resources/application.yaml`).

## Dateien
- `app/src/main/java/dev/krocon/shop/ep011/jdbc/OrderDao.java` – Problem: `SELECT * FROM orders`, `while (rs.next())`, Mapping von Hand; `findAllWithTypo()` mit `"totl"` wie im Video.
- `app/src/main/java/dev/krocon/shop/ep011/jdbc/JdbcOrder.java` – Datenbehälter mit Settern, `customerId` ist nur ein `Long`.
- `app/src/main/java/dev/krocon/shop/domain/Order.java` – Lösung: Entity `Order` → Tabelle `orders`, `@ManyToOne(fetch = LAZY) customer` → `customer_id`.
- `app/src/test/java/dev/krocon/shop/ep011/OrderRepositoryTest.java` – Hand-Mapping, Tippfehler, Entity laden, Unit of Work (Änderung → UPDATE erst beim Flush).

## Was du sehen solltest
- Hand-Mapping liefert 5.000 Bestellungen; mit `"totl"` knallt es erst zur Laufzeit:
  `PSQLException: The column name totl was not found in this ResultSet.`
- `em.find(Order.class, 1001L)` → **1 Query**
  `select o1_0.id,o1_0.created_at,o1_0.customer_id,o1_0.status,o1_0.total from orders o1_0 where o1_0.id=?`;
  `order.getCustomer()` ist ein `Customer`-Proxy (ID ohne Query), erst `getEmail()` lädt den Kunden (**2. Query**).
- `order.setStatus(SHIPPED)` erzeugt **0 Queries**; erst `em.flush()` schickt
  `update orders set created_at=?,customer_id=?,status=?,total=? where id=?` (1 Entity-Update laut Statistik).
