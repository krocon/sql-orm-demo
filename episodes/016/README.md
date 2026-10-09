# Folge 016 – Lazy vs. Eager Loading   (EN: Lazy vs. eager loading)
Video: „Lazy vs. Eager Loading in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Lazy lädt eine Beziehung erst beim Zugriff – das braucht eine offene Session. Nach Transaktionsende
(open-in-view ist `false`) knallt der erste Zugriff mit `LazyInitializationException`. Die JPA-Defaults sind
tückisch: To-One ist EAGER, To-Many LAZY. Lösung: alles LAZY, pro Use Case gezielt per JOIN FETCH oder Entity Graph laden.

## Ausprobieren
```bash
cd app
./mvnw test -Dtest='dev.krocon.shop.ep016.**'     # PostgreSQL 18 per Testcontainers (Docker nötig)

# alternativ gegen die Compose-Datenbank (Flyway legt Schema + Testdaten in shop_app an):
docker compose up -d                             # im Repo-Wurzelverzeichnis, .env aus .env.example
DB_URL=jdbc:postgresql://127.0.0.1:5433/shop_app POSTGRES_USER=shop POSTGRES_PASSWORD=change-me \
  ./mvnw test -Dtest='dev.krocon.shop.ep016.**'
```

## Dateien
- `app/src/main/java/dev/krocon/shop/domain/Order.java` – `@ManyToOne(fetch = FetchType.LAZY) customer`.
- `app/src/main/java/dev/krocon/shop/ep016/Ep016OrderRepository.java` – `findWithCustomer` (JOIN FETCH, wie im Video) und `findWithCustomerGraphById` (`@EntityGraph`).
- `app/src/main/java/dev/krocon/shop/ep016/OrderService.java` – `find(id)` (Problem) und `findWithCustomer(id)` (Lösung), je `@Transactional(readOnly = true)`.
- `app/src/main/java/dev/krocon/shop/ep016/EagerDefaultOrder.java` – dieselbe Tabelle `orders`, `@ManyToOne` **ohne** fetch-Angabe.
- `app/src/test/java/dev/krocon/shop/ep016/LazyInitTest.java` – Zugriff nach Transaktionsende → Exception; JOIN FETCH und Entity Graph → kein Fehler.
- `app/src/test/java/dev/krocon/shop/ep016/EagerDefaultTest.java` – Defaults nachgemessen: ToOne EAGER, ToMany LAZY.

## Was du sehen solltest
- `orderService.find(1001L).getCustomer().getName()` →
  `LazyInitializationException: Could not initialize proxy [dev.krocon.shop.domain.Customer#854] - no session`
- Mit `findWithCustomer` bzw. Entity Graph: 1 Query `… from orders o1_0 join customers c1_0 on c1_0.id=o1_0.customer_id where o1_0.id=?`, kein Fehler.
- `@ManyToOne` ohne `fetch`: `em.find` lädt den Kunden **immer** mit – 1 Query mit
  `left join customers`, 2 Entities geladen, Customer ist initialisiert.
- `@ManyToOne(fetch = LAZY)`: Customer ist ein Proxy, erst der Zugriff kostet die 2. Query.
- `Customer.orders` (`@OneToMany` ohne fetch-Angabe) ist nach `find` nicht initialisiert.
