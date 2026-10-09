# Folge 038 – ORM-Anti-Patterns            (EN: Common ORM anti-patterns)
Video: „ORM-Anti-Patterns in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Vier typische Fehler, jeweils als Test: To-One-Beziehungen bleiben EAGER (N+1), Entities werden direkt als API-Modell ausgeliefert, `CascadeType.ALL` sitzt auf einer `@ManyToOne`, und Reports laufen per `findAll()` durch tausende Entities. Gegenmittel: LAZY + gezieltes Fetchen, Records als DTOs, Cascade nur vom Parent zu seinen Kindern, SQL-Projektion/Paging/Bulk.

## Ausprobieren
```bash
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep038.**'
# ohne Docker: docker compose up -d, .env laden, DB_URL=jdbc:postgresql://localhost:5433/shop_app setzen
```

## Dateien
- `app/src/main/java/dev/krocon/shop/ep038/bad/Order.java` – Anti-Pattern: `@ManyToOne(cascade = CascadeType.ALL)` (implizit EAGER), gleiche Tabelle `orders`
- `app/src/main/java/dev/krocon/shop/domain/Order.java` – die gute Variante (LAZY, ohne Cascade auf `customer`, `cascade = ALL, orphanRemoval = true` nur auf `items`)
- `app/src/main/java/dev/krocon/shop/ep038/OrderDto.java` – Record als API-Modell
- `app/src/main/java/dev/krocon/shop/ep038/OrderController.java` – `GET /ep038/orders?page=0` (DTOs, paginiert), `GET /ep038/reports/revenue`
- `app/src/main/java/dev/krocon/shop/ep038/ReportRepository.java` – `findAll` + Summieren in Java vs. JPQL-Projektion mit `group by`
- `app/src/main/java/dev/krocon/shop/ep038/StatusRevenue.java` – Ergebnis-Record des Reports
- `app/src/test/java/dev/krocon/shop/ep038/AntiPatternsTest.java` – alle vier Anti-Patterns mit Query-/Entity-Zählung

## Was du sehen solltest
- EAGER: `select o from Ep038BadOrder o where o.id <= 10` → **11 Statements** (1 + 10 Kunden); dieselbe Query auf der LAZY-`Order` → **1 Statement**.
- Entity als API: Jackson serialisiert `Order` → lädt `customer` und dessen `orders` nach (3 Statements) und scheitert dann an der Rekursion Order → Customer → orders → Order (`JacksonException`, Tiefe 500). Das DTO ergibt `{"id":1,"status":"PAID","total":203.59,"customerId":927}` ohne Query (die Kunden-ID steckt im Proxy).
- Cascade ALL auf `@ManyToOne`: `em.remove(order)` erzeugt `delete from orders …` **und** `delete from customers …` → `update or delete on table "customers" violates foreign key constraint "orders_customer_id_fkey"`. Mit der guten Order: nur `order_items` + `orders` werden gelöscht, der Kunde bleibt.
- Report: `findAll()` lädt **5000 Entities**, die Projektion liefert 4 Zeilen ohne eine einzige Entity – gleiche Summen (CANCELLED 188 817,25 · NEW 211 765,43 · PAID 420 548,76 · SHIPPED 210 266,68).
