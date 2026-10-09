# Folge 029 – DTOs vs. Entities            (EN: DTOs vs. Entities)
Video: „DTOs vs. Entities in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Gibt ein Controller die `Order`-Entity direkt als JSON zurück, läuft Jackson über `customer → orders → customer …` in eine Endlosschleife – oder scheitert ohne offene Session an einem Lazy-Proxy. Nimmt er eine Entity als Request Body an, setzt der Client Felder wie `total` (Mass Assignment). Lösung: Records als Request-/Response-DTOs und Lesezugriffe direkt per `select new` projizieren.

## Ausprobieren
```bash
cd app
./mvnw test -Dtest='dev.krocon.shop.ep029.**'          # Testcontainers (Docker)
# oder gegen die Compose-DB:
DB_URL=jdbc:postgresql://localhost:5433/shop_app ./mvnw test -Dtest='dev.krocon.shop.ep029.**'
```
Die Tests rufen die Controller-Methoden direkt auf und serialisieren mit Jacksons `JsonMapper` – genau das macht Spring MVC beim Rendern.

## Dateien
- `app/src/main/java/dev/krocon/shop/ep029/BadOrderController.java` – so nicht: `Order` als JSON raus, als `@RequestBody` rein
- `app/src/main/java/dev/krocon/shop/ep029/OrderController.java` – so: DTO raus, `CreateOrderRequest` rein, `total` rechnet der Server
- `app/src/main/java/dev/krocon/shop/ep029/OrderDto.java` – Response-Record `(id, status, total)`
- `app/src/main/java/dev/krocon/shop/ep029/CreateOrderRequest.java` – Request-Record ohne `id`/`status`/`total`
- `app/src/main/java/dev/krocon/shop/ep029/OrderMapper.java` – manuelles Mapping, MapStruct-Variante im Kommentar
- `app/src/main/java/dev/krocon/shop/ep029/Ep029OrderRepository.java` – `findDtosByCustomer` mit `select new …OrderDto(o.id, o.status, o.total)`
- `app/src/test/java/dev/krocon/shop/ep029/EntityAsJsonTest.java` – beweist Schleife, Lazy-Fehler, Mass Assignment und die Projektion

## Was du sehen solltest
- Endlosschleife (in der Transaktion): `Document nesting depth (501) exceeds the maximum allowed (500 …) (through reference chain: …Order["customer"]->…Customer$HibernateProxy["orders"]->…PersistentBag[0]->…Order["customer"] …` – Jackson 3 bricht bei Tiefe 500 ab statt mit StackOverflowError.
- Ohne Transaktion (open-in-view: false): `Could not initialize proxy [dev.krocon.shop.domain.Customer#927] - no session` (LazyInitializationException).
- Mass Assignment: JSON `{"status":"PAID","total":0.01}` → gespeicherte Summe 0.01.
- DTO-JSON: `{"id":1,"status":"PAID","total":203.59}` – nur drei Felder.
- Projektion: genau 1 Statement, 0 geladene Entities, SQL `select o1_0.id,o1_0.status,o1_0.total from orders o1_0 where o1_0.customer_id=?`.
