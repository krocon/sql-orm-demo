# Folge 040 – Alternativen: jOOQ, MyBatis, JDBC            (EN: ORM alternatives: jOOQ, MyBatis and JDBC compared)
Video: „Alternativen: jOOQ, MyBatis, JDBC in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Der Umsatzreport lädt mit JPA alle Bestellungen als Entities und summiert in Java – die Datenbank kann das mit einem `GROUP BY`. Derselbe Report hier mit Spring `JdbcClient` (SQL → Record), MyBatis (SQL im XML-Mapper) und jOOQ (typsicheres DSL). JPA bleibt für Domänenmodell und CRUD; Mischen in einer Transaktion ist legitim – mit einem `flush()` an der richtigen Stelle.

## Ausprobieren
```bash
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep040.**'
# ohne Docker: docker compose up -d, .env laden, DB_URL=jdbc:postgresql://localhost:5433/shop_app setzen
```

## Dateien
- `app/src/main/java/dev/krocon/shop/ep040/StatusRevenue.java` – Record `(OrderStatus status, BigDecimal revenue)`
- `app/src/main/java/dev/krocon/shop/ep040/RevenueJdbc.java` – Report mit `JdbcClient` (Code wie im Video)
- `app/src/main/java/dev/krocon/shop/ep040/RevenueMapper.java` + `app/src/main/resources/dev/krocon/shop/ep040/RevenueMapper.xml` – dasselbe mit MyBatis (Konstruktor-Mapping für das Record)
- `app/src/main/java/dev/krocon/shop/ep040/RevenueJooq.java` – jOOQ-DSL wie im Video (`ORDERS.STATUS`), zusätzlich ohne Codegen (`field(name("status"))`) und eine Window Function
- `app/src/main/java/dev/krocon/shop/ep040/jooq/Orders.java` – handgeschriebener Ausschnitt dessen, was jOOQs Codegen erzeugen würde (siehe unten)
- `app/src/main/java/dev/krocon/shop/ep040/OrderService.java` – JPA schreibt, JdbcClient liest in derselben `@Transactional`-Methode
- `app/src/test/java/dev/krocon/shop/ep040/RevenueReportTest.java` – alle Varianten liefern dieselben Summen; `findAll` lädt 5000 Entities; Flush-Falle beim Mischen

Dependencies (in `app/pom.xml`; jOOQ-Version 3.21.5 aus der Boot-BOM, MyBatis-Starter 4.1.0 ist für Spring Boot 4.1 gebaut):
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-jooq</artifactId>
</dependency>
<dependency>
  <groupId>org.mybatis.spring.boot</groupId>
  <artifactId>mybatis-spring-boot-starter</artifactId>
  <version>4.1.0</version>
</dependency>
```

### jOOQ-Codegen (woher `ORDERS.STATUS` kommt)
Im Video stammen `ORDERS`, `ORDERS.STATUS`, `ORDERS.TOTAL` aus generierten Klassen. Der Generator liest das Schema – am besten aus einer echten, von Flyway migrierten Datenbank – und schreibt pro Tabelle eine Klasse (`Tables.ORDERS`, `OrdersRecord` …). Beispiel mit `jooq-codegen-maven` gegen die Compose-Datenbank (`./mvnw generate-sources` nach `docker compose up -d` und Flyway-Lauf):
```xml
<plugin>
  <groupId>org.jooq</groupId>
  <artifactId>jooq-codegen-maven</artifactId>  <!-- Version aus dem Boot-Parent -->
  <executions><execution><goals><goal>generate</goal></goals></execution></executions>
  <dependencies>
    <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><version>${postgresql.version}</version></dependency>
  </dependencies>
  <configuration>
    <jdbc>
      <url>jdbc:postgresql://localhost:5433/shop_app</url>
      <user>${env.POSTGRES_USER}</user><password>${env.POSTGRES_PASSWORD}</password>
    </jdbc>
    <generator>
      <database><inputSchema>public</inputSchema><excludes>flyway_schema_history</excludes></database>
      <target><packageName>dev.krocon.shop.jooq</packageName></target>
    </generator>
  </configuration>
</plugin>
```
Alternativen: `org.jooq.meta.extensions.ddl.DDLDatabase` (liest die `V*.sql`-Skripte direkt, versteht aber nicht jede PostgreSQL-Syntax) oder Flyway + Testcontainers im Build. Ohne Codegen geht jOOQ auch per `DSL.table(name("orders"))`/`DSL.field(name("status"), String.class)` – dann ohne Typsicherheit gegenüber dem Schema.

## Was du sehen solltest
- JPA `findAll`: **5000 Entities** geladen für 4 Summen.
- JdbcClient, MyBatis, jOOQ (mit und ohne Codegen): je 4 Zeilen, identisch – CANCELLED 188 817,25 · NEW 211 765,43 · PAID 420 548,76 · SHIPPED 210 266,68 – und 0 geladene Entities.
- jOOQ-SQL der Window Function:
  `select "orders"."status", sum("orders"."total") as "revenue", (sum("orders"."total") / sum(sum("orders"."total")) over ()) as "share" from "orders" group by "orders"."status"`
- Mischen: Nach `order.setStatus(CANCELLED)` sieht der JdbcClient-Report die Änderung **nur nach `em.flush()`** – JDBC läuft an Hibernates Auto-Flush vorbei (der greift nur vor HQL/nativen Queries über Hibernate).

> **Java-Version:** Die Open-Source-Ausgabe von jOOQ braucht ab 3.20 Java 21. Damit das Demo-Repo auf Java 17 läuft, legt `app/pom.xml` `jooq.version` auf **3.19.30** fest. Mit Java 21+ kannst du die Zeile entfernen und bekommst die Version aus dem Spring-Boot-BOM (3.21.x).
