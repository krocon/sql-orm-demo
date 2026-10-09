# Folge 035 – Open Session in View            (EN: Open Session in View)
Video: „Open Session in View in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Mit Open Session in View (OSIV, in Spring Boot standardmäßig an) bleibt der Persistence Context bis zum Ende des Requests offen: Gibt der Controller eine Entity zurück, feuert der JSON-Serializer versteckte Lazy-Queries. Mit `spring.jpa.open-in-view: false` fällt das sofort als `LazyInitializationException` auf – die Lösung ist ein Service, der per `JOIN FETCH` lädt und ein DTO (Record) zurückgibt. Die Session pro Request bei FastAPI (`Depends(get_db)`) ist dasselbe Muster.

## Ausprobieren
```bash
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep035.**'        # mit Docker (Testcontainers)
# oder gegen die Compose-Datenbank:
docker compose up -d && set -a && . ./.env && set +a
cd app && DB_URL=jdbc:postgresql://localhost:5433/shop_app ./mvnw test -Dtest='dev.krocon.shop.ep035.**'
```
Umschalten in der App: `spring.jpa.open-in-view` in `app/src/main/resources/application.yaml` (Repo-Stand: `false`).

## Dateien
- `app/src/main/java/dev/krocon/shop/ep035/OrderController.java` – `GET /ep035/orders/{id}` gibt die Entity zurück (Problem), `GET /ep035/orders/{id}/view` das DTO (Lösung)
- `app/src/main/java/dev/krocon/shop/ep035/OrderService.java` – `findById` (Entity raus) vs. `findWithItemsById` (JOIN FETCH → `OrderView`)
- `app/src/main/java/dev/krocon/shop/ep035/OrderView.java` – DTO-Record mit Positionen
- `app/src/test/java/dev/krocon/shop/ep035/OsivTest.java` – OSIV an: versteckte Queries beim JSON-Rendern (Hibernate Statistics)
- `app/src/test/java/dev/krocon/shop/ep035/OsivOffTest.java` – OSIV aus: `LazyInitializationException` im „Controller-Teil“, DTO-Endpoint mit 1 Statement
- `app/src/test/java/dev/krocon/shop/ep035/OsivWarningTest.java` – prüft die Startwarnung, wenn `open-in-view` nicht gesetzt ist
- `app/src/test/resources/ep035-osiv-unset.yaml` – wie `application.yaml`, nur ohne `open-in-view`

## Was du sehen solltest
- Startwarnung von Spring Boot 4.1 (Logger `JpaBaseConfiguration$JpaWebConfiguration`, WARN), wenn die Property fehlt:
  `spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning`
- OSIV an, `GET /ep035/orders/1`: **3 Statements** statt 1 – `orders`, dann beim Serialisieren `customers` und die `orders` des Kunden. Danach bricht Jackson ab: `Document nesting depth (501) exceeds the maximum allowed (500)` – Order → Customer → orders → Order … ist eine Endlos-Rekursion. Status ist trotzdem 200, der Body (~30 KB) ist kaputtes JSON.
- OSIV aus, gleicher Endpoint: 1 Statement, dann `HttpMessageNotWritableException` mit Ursache `LazyInitializationException` – der Fehler fällt sofort auf.
- `service.findById(1L).getItems().size()` außerhalb der Transaktion → `LazyInitializationException`.
- DTO-Endpoint: **1 Statement** (`orders join customers join order_items join products`), Antwort mit 2 Positionen.
