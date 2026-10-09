# Folge 036 – Auditing & Soft Deletes            (EN: Auditing & soft deletes)
Video: „Auditing & Soft Deletes in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Spring Data füllt mit `@EnableJpaAuditing` die Felder `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy` (Benutzer via `AuditorAware`), Hibernate Envers schreibt mit `@Audited` jede Version in eine `_aud`-Tabelle, und Hibernates `@SoftDelete` (seit 6.4) markiert Zeilen nur als gelöscht und filtert sie aus allen JPA-Abfragen. Die Fallen: Unique-Constraints (→ Partial Index), natives SQL filtert nicht mit, und die DSGVO verlangt echtes Löschen – auch in der Historie.

## Ausprobieren
```bash
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep036.**'
# ohne Docker: docker compose up -d, .env laden, DB_URL=jdbc:postgresql://localhost:5433/shop_app setzen
```

## Dateien
- `app/src/main/java/dev/krocon/shop/ep036/Order.java` – eigene Entity `ep036_orders`: Auditing-Felder + `@SoftDelete` + `@Audited` (die geteilte `domain/Order` bleibt unverändert)
- `app/src/main/java/dev/krocon/shop/ep036/Customer.java` – `ep036_customers` mit `@SoftDelete` für die Unique-Falle
- `app/src/main/java/dev/krocon/shop/ep036/AuditConfig.java` – `@EnableJpaAuditing` + `AuditorAware` (Spring-Security-Variante im Kommentar)
- `app/src/main/java/dev/krocon/shop/ep036/CurrentUser.java` – ThreadLocal statt Spring Security (Demo)
- `app/src/main/java/dev/krocon/shop/ep036/OrderRepository.java` – Spring-Data-Repository, Query ohne `deleted`-Bedingung
- `app/src/main/resources/db/migration/V350__ep036_auditing_softdelete.sql` – Tabellen inkl. `revinfo`, `revinfo_seq` (Schrittweite 50) und `ep036_orders_aud`
- `app/src/main/resources/db/migration/V351__ep036_partial_unique_email.sql` – `UNIQUE … WHERE NOT deleted`
- `app/src/test/java/dev/krocon/shop/ep036/AuditingSoftDeleteTest.java` – Auditing-Felder, Soft-Delete-Filter, Partial-Unique-Index
- `app/src/test/java/dev/krocon/shop/ep036/OrderHistoryTest.java` – Envers-Revisionen einer Bestellung (echte Commits)

Dependency (in `app/pom.xml`, Version aus der Boot-BOM = 7.4.1.Final):
```xml
<dependency>
  <groupId>org.hibernate.orm</groupId>
  <artifactId>hibernate-envers</artifactId>
</dependency>
```

## Was du sehen solltest
- Auditing: `createdBy = anna`, nach Änderung durch ben `updatedBy = ben`, `updatedAt > createdAt`.
- `@SoftDelete` (Paket `org.hibernate.annotations`, Standard: `strategy = DELETED`, Spalte `deleted boolean`):
  - `delete` wird zu `update ep036_orders set deleted=true where id=? and deleted=false`
  - jede JPA-Query bekommt `where o1_0.deleted=false` – auch die eigene JPQL ohne Bedingung
  - natives SQL (`select count(*) from ep036_orders …`) zählt die gelöschte Zeile weiter mit → Reports per SQL müssen selbst filtern
- Envers: drei Revisionen `[ADD, MOD, DEL]` für eine Bestellung, `reader.find(…, rev1)` liefert den alten Status NEW. Envers schreibt pro Commit `insert into revinfo` + `insert into ep036_orders_aud`. Die `_aud`-Tabelle hat keine `deleted`-Spalte – das Soft Delete erscheint als Revision vom Typ DEL.
- Unique-Falle: nach dem Soft Delete darf `lisa@shop.de` sich neu registrieren (Partial Index), ein zweiter aktiver Datensatz scheitert an `ep036_customers_email_active_uq`.
- Mit `ddl-auto: validate` müssen `revinfo`, `revinfo_seq` und `…_aud` per Migration existieren – sonst startet die App nicht.
