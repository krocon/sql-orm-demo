# Folge 019 – JPA vs. Hibernate            (EN: JPA vs. Hibernate)
Video: „JPA vs. Hibernate in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
JPA (heute Jakarta Persistence 3.2) ist nur die Spezifikation: Annotationen und `EntityManager`. Hibernate 7 ist die Implementierung, die Spring Boot mitbringt. Der Shop-Code nutzt nur `jakarta.persistence.*`; Hibernate-Extras wie `@BatchSize` oder die `Session` kommen bewusst und an genau einer Stelle dazu.

## Ausprobieren
```bash
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep019.**'   # PostgreSQL 18 per Testcontainers (Docker nötig)
# alternativ gegen die Compose-Datenbank (docker compose up -d im Repo-Wurzelverzeichnis):
DB_URL=jdbc:postgresql://127.0.0.1:5433/shop_app POSTGRES_USER=shop POSTGRES_PASSWORD=change-me \
  ./mvnw test -Dtest='dev.krocon.shop.ep019.**'
```
Abhängigkeiten ansehen: `cd app && ./mvnw dependency:tree -Dincludes=jakarta.persistence,org.hibernate.orm`

## Dateien
- `app/src/main/java/dev/krocon/shop/ep019/Ep019Order.java` – reine JPA-Annotationen plus bewusst markiertes `@BatchSize(size = 20)` (Tabelle `orders`)
- `app/src/main/java/dev/krocon/shop/ep019/Ep019OrderItem.java` – Position mit Lazy-`@ManyToOne` auf `Ep019Order`, damit `@BatchSize` sichtbar wird
- `app/src/main/java/dev/krocon/shop/ep019/UnwrapSession.java` – die einzige Stelle mit `em.unwrap(Session.class)` (Hibernate-`findMultiple`)
- `app/src/test/java/dev/krocon/shop/ep019/JpaVsHibernateTest.java` – Versionen ausgeben, `javax` fehlt, `domain/` ohne Hibernate-Imports, `@BatchSize` messen
- `app/pom.xml` – `spring-boot-starter-data-jpa` zieht `jakarta.persistence-api` (API) und `hibernate-core` (Implementierung)

## Was du sehen solltest
```
Jakarta Persistence API: jakarta.persistence-api-3.2.0.jar
Hibernate ORM:           7.4.1.Final
EntityManager dahinter:  org.hibernate.internal.SessionImpl
Hibernate-Extra in: src/main/java/dev/krocon/shop/ep019/Ep019Order.java
Lazy-Orders laden: ohne @BatchSize 40 Queries, mit @BatchSize 2
```
- `Class.forName("javax.persistence.Entity")` → `ClassNotFoundException`: alter `javax`-Code kompiliert nicht mehr.
- Mit `@BatchSize` lädt Hibernate die 40 Lazy-Orders in 2 Abfragen: `select … from orders eo1_0 where eo1_0.id = any (?)` (je 20 IDs als Array).
- Die Dateien in `domain/` enthalten weder `javax.persistence` noch `org.hibernate`.
