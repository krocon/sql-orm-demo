# Folge 015 – Beziehungen: 1:1, 1:n, n:m   (EN: Entity relationships: one-to-one, one-to-many, many-to-many)
Video: „Beziehungen: 1:1, 1:n, n:m in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Der Fremdschlüssel liegt immer auf der n-Seite (`orders.customer_id`), und diese Owning Side bestimmt, was
geschrieben wird. Ein `@ManyToMany` erzeugt eine Join-Tabelle mit nur zwei IDs – für Menge und Stückpreis
ist kein Platz. Deshalb wird n:m mit Daten zur eigenen Entity `OrderItem`.

## Ausprobieren
```bash
cd app
./mvnw test -Dtest='dev.krocon.shop.ep015.**'     # PostgreSQL 18 per Testcontainers (Docker nötig)

# alternativ gegen die Compose-Datenbank (Flyway legt Schema + Testdaten in shop_app an):
docker compose up -d                             # im Repo-Wurzelverzeichnis, .env aus .env.example
DB_URL=jdbc:postgresql://127.0.0.1:5433/shop_app POSTGRES_USER=shop POSTGRES_PASSWORD=change-me \
  ./mvnw test -Dtest='dev.krocon.shop.ep015.**'
```

## Dateien
- `app/src/main/java/dev/krocon/shop/domain/Order.java` – `@ManyToOne(fetch = LAZY) customer` (Owning Side), `@OneToMany(mappedBy = "order") items`.
- `app/src/main/java/dev/krocon/shop/domain/OrderItem.java` – n:m-Auflösung mit `quantity` und `unitPrice`.
- `app/src/main/java/dev/krocon/shop/ep015/ManyToManyOrder.java` – Problem: dieselbe Tabelle `orders`, aber `@ManyToMany List<Product> products`.
- `app/src/main/resources/db/migration/V115__ep015_order_products.sql` – Join-Tabelle `ep015_order_products (order_id, product_id)`.
- `app/src/test/java/dev/krocon/shop/ep015/RelationshipsTest.java` – Join-Tabelle hat nur 2 Spalten, `OrderItem` trägt die Daten, FK auf der n-Seite, nur die Owning Side zählt.
- Tabellen mit Fremdschlüsseln: `schema/shop.sql`.

## Was du sehen solltest
- `@ManyToMany` + `flush` → `insert into ep015_order_products (order_id,product_id) values (?,?)`;
  die Tabelle hat genau die Spalten `order_id`, `product_id`.
- Bestellung 1001: jede `OrderItem` hat Produkt, `quantity` > 0 und `unitPrice`.
- `customer_id` gibt es in `orders`, nicht in `customers`.
- Bestellung nur in `customer.getOrders()` umhängen (inverse Seite, `mappedBy`) → **kein UPDATE**, Bestellung 1001
  gehört danach weiter dem alten Kunden.
