# Folge 026 – Flush vs. Commit            (EN: Flush vs. commit)
Video: „Flush vs. Commit in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Flush schickt die gesammelten SQL-Anweisungen an die DB – die Transaktion läuft weiter und ist noch rollbackbar. Commit schließt sie ab. Mit `SEQUENCE`-IDs kommt das INSERT (und damit ein UNIQUE-Fehler) erst beim Flush, im Zweifel beim Commit – nach der Willkommensmail. Lösung: `saveAndFlush()` bzw. Mail per `@TransactionalEventListener(AFTER_COMMIT)`.

## Ausprobieren
```bash
cd app && ./mvnw test -Dtest='dev.krocon.shop.ep026.**'
```

## Dateien
- `app/src/main/java/dev/krocon/shop/ep026/Ep026Customer.java` – Kunde mit `SEQUENCE`-ID (wie im Video: `save()` schickt noch kein INSERT)
- `app/src/main/java/dev/krocon/shop/ep026/RegisterService.java` – `register()` (Problem), `registerAndFlush()`, `registerAfterCommit()`
- `app/src/main/java/dev/krocon/shop/ep026/WelcomeMailListener.java` – Mail per `@TransactionalEventListener(AFTER_COMMIT)`
- `app/src/main/java/dev/krocon/shop/ep026/MailService.java`, `CustomerRegistered.java`, `Ep026CustomerRepository.java`
- `app/src/main/resources/db/migration/V260__ep026_customers.sql` – Tabelle mit `email UNIQUE`, Sequenz
- `app/src/test/java/dev/krocon/shop/ep026/FlushDemoTest.java` – INSERT erst bei `flush()`, FlushMode AUTO vor JPQL/Native
- `app/src/test/java/dev/krocon/shop/ep026/UniqueEmailTest.java` – `save` wirft erst beim Commit, `saveAndFlush` sofort (echte Commits)

## Was du sehen solltest
- `persist()` → 0 INSERTs, `flush()` → 1 INSERT; die Zeile ist danach in derselben Transaktion per JDBC sichtbar.
- `register()` mit vergebener E-Mail: `DataIntegrityViolationException` beim Commit – die Mail steht trotzdem in `MailService.sent()`.
- `registerAndFlush()`: Fehler sofort, keine Mail. `registerAfterCommit()`: Mail nur, wenn der Commit klappt.
- JPQL auf `Ep026Customer` flusht automatisch vorher; eine Abfrage auf `Product` nicht. Auch vor einer Native Query flusht Hibernate 7 automatisch (`count = 1`); `JdbcTemplate` umgeht Hibernate und sieht ohne `flush()` nichts.
- Gegenprobe mit `domain.Customer` (IDENTITY): `save()` INSERTet sofort und wirft den UNIQUE-Fehler schon dort.
