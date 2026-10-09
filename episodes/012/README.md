# Folge 012 – Der Impedance Mismatch   (EN: The object-relational impedance mismatch)
Video: „Der Impedance Mismatch in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Java kennt Objekte mit Identität, Vererbung, gerichteten Referenzen und Listen; die Datenbank kennt flache
Tabellen, Primär- und Fremdschlüssel. Die Tests zeigen die vier Lücken, die das ORM überbrücken muss:
Identität, Vererbung, Assoziationen (n:m nur über `order_items`) und Navigation (Getter-Kette vs. JOIN).

## Ausprobieren
```bash
cd app
./mvnw test -Dtest='dev.krocon.shop.ep012.**'     # PostgreSQL 18 per Testcontainers (Docker nötig)

# alternativ gegen die Compose-Datenbank (Flyway legt Schema + Testdaten in shop_app an):
docker compose up -d                             # im Repo-Wurzelverzeichnis, .env aus .env.example
DB_URL=jdbc:postgresql://127.0.0.1:5433/shop_app POSTGRES_USER=shop POSTGRES_PASSWORD=change-me \
  ./mvnw test -Dtest='dev.krocon.shop.ep012.**'
```
Nur SQL: `psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/012/navigation.sql`

## Dateien
- `app/src/test/java/dev/krocon/shop/ep012/IdentityTest.java` – zwei `find()` im selben Persistence Context → `==`; in zwei Kontexten → zwei Objekte, `equals` false, gleiche ID.
- `app/src/test/java/dev/krocon/shop/ep012/NavigationTest.java` – `order.getCustomer().getEmail()` vs. JOIN-SQL aus dem Video; Produkte der Bestellung über `order_items`.
- `app/src/test/java/dev/krocon/shop/ep012/InheritanceTest.java` – polymorphe Abfrage über eine Klassenhierarchie.
- `app/src/main/java/dev/krocon/shop/ep012/model/` – `Payment` (abstrakt), `CardPayment`, `InvoicePayment`: `@Inheritance(SINGLE_TABLE)` mit Diskriminator.
- `app/src/main/resources/db/migration/V112__ep012_payments.sql` – Tabelle `ep012_payments` für die Hierarchie.
- `episodes/012/navigation.sql` – Getter-Kette und n:m als reines SQL.
- Objektmodell/Tabellen aus dem Video: `app/src/main/java/dev/krocon/shop/domain/` bzw. `schema/shop.sql`.

## Was du sehen solltest
- Identität: im selben Kontext `a == b` → true; mit zweitem `EntityManager` → `a == b` false,
  `a.equals(b)` false (kein eigenes `equals`), `a.getId().equals(b.getId())` true (PK 42).
- Navigation: Getter-Kette = **2 Queries** (orders, dann customers), JOIN = **1 Query**;
  beide liefern `kunde854@shop.example` für Bestellung 1001.
- n:m: Bestellung 1001 hat die Produkte `SKU-016` und `SKU-049` – in Java eine Liste, in SQL ein JOIN über `order_items`.
- Vererbung: `select p from Ep012Payment p` liefert `CardPayment` und `InvoicePayment`; in der Tabelle
  steht `type = CARD | INVOICE`, nicht genutzte Spalten sind `NULL`.
