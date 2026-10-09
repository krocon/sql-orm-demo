# Folge 031 – Migrationen: Flyway & Liquibase            (EN: Schema Migrations: Flyway & Liquibase)
Video: „Migrationen: Flyway & Liquibase in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
`ddl-auto: update` ändert das Schema „irgendwie“ – ohne Verlauf, Review oder Plan B. Mit Flyway liegt jede Änderung als versionierte SQL-Datei im Git, Flyway führt sie der Reihe nach aus und merkt sich den Stand samt Checksumme in `flyway_schema_history`. Hibernate prüft mit `ddl-auto: validate` nur noch, ob das Mapping passt.

## Ausprobieren
```bash
cd app
./mvnw test -Dtest='dev.krocon.shop.ep031.**'
# Stand der App-Datenbank ansehen (Compose):
docker compose exec -T db psql -U shop -d shop_app -c "select version, description, checksum, success from flyway_schema_history order by installed_rank"
```

## Regeln
1. **Nie ändern, was angewendet ist.** Flyway speichert pro Migration eine Checksumme; eine geänderte Datei bricht beim nächsten Start mit `Migration checksum mismatch` ab.
2. **Vorwärts korrigieren.** Fehler werden mit einer neuen Migration behoben (hier `V2_1__fix_status_default.sql`), nicht durch Editieren von V2.
3. **Expand/Contract** für Änderungen ohne Downtime: erst neue Spalte ergänzen und Daten kopieren (Expand), Code umstellen, erst im nächsten Release die alte Spalte entfernen (Contract).
4. **`ddl-auto: validate`** in allen Umgebungen; Migrationen im Test gegen echtes PostgreSQL (Testcontainers) laufen lassen.

## Dateien
- `app/src/main/resources/db/migration/V300__ep031_create_customer_contacts.sql` – eigene Tabelle `ep031_customer_contacts` mit Freitext-`phone`
- `app/src/main/resources/db/migration/V301__ep031_add_customer_phone.sql` – Expand: neue Spalte `phone_e164` neben `phone`, Daten kopiert; Contract folgt später
- `app/src/main/java/dev/krocon/shop/ep031/Ep031CustomerContact.java` – Mapping, das `ddl-auto: validate` gegen V300/V301 prüft
- `app/src/test/resources/ep031/flyway/V1__create_orders.sql`, `V2__add_order_status.sql` – die Migrationen aus dem Video (laufen im eigenen Schema `ep031_demo`)
- `app/src/test/resources/ep031/flyway/V2_1__fix_status_default.sql` – Vorwärts-Korrektur
- `app/src/test/resources/ep031/flyway/V3__expand_add_order_state.sql`, `V4__expand_copy_status_to_state.sql`, `V5__contract_drop_status.sql` – Expand/Contract in drei Schritten
- `app/src/test/java/dev/krocon/shop/ep031/MigrationTest.java` – App-Historie prüfen; Video-Beispiel migrieren, Checksummen-Fehler provozieren, vorwärts korrigieren, Expand/Contract
- `episodes/031/liquibase/db.changelog-master.yaml` – dieselbe Änderung als Liquibase-changeSet mit `rollback` (nur Beispiel, nicht eingebunden)

## Was du sehen solltest
- Beim Start: `Migrating schema "public" to version "300 - ep031 create customer contacts"` und `"301 - ep031 add customer phone"`.
- Nach V301: `phone_e164` = `+49401234567`, `+4930987654`, `NULL` (alte Spalte `phone` bleibt).
- V2 nachträglich geändert: `Validate failed: … Migration checksum mismatch for migration version 2 -> Applied to database : 1467695154 -> Resolved locally : -1078839079`.
- Danach: V2.1 (Korrektur), V3 + V4 (Spalten `status` und `state` parallel, 3 von 3 Zeilen kopiert), V5 (nur noch `state`).
