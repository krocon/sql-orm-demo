# Folge 014 – Entity Lifecycle: New, Managed, Detached, Removed   (EN: Entity lifecycle: new, managed, detached, removed)
Video: „Entity Lifecycle: New, Managed, Detached, Removed in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Nur managed Entities werden per Dirty Checking gespeichert. Lädt ein Service einen Kunden und die
Transaktion endet (open-in-view ist `false`), ist die Entity detached – eine Änderung danach geht
still verloren. `merge` gibt eine *neue* managed Instanz zurück, das Original bleibt detached.

## Ausprobieren
```bash
cd app
./mvnw test -Dtest='dev.krocon.shop.ep014.**'     # PostgreSQL 18 per Testcontainers (Docker nötig)

# alternativ gegen die Compose-Datenbank (Flyway legt Schema + Testdaten in shop_app an):
docker compose up -d                             # im Repo-Wurzelverzeichnis, .env aus .env.example
DB_URL=jdbc:postgresql://127.0.0.1:5433/shop_app POSTGRES_USER=shop POSTGRES_PASSWORD=change-me \
  ./mvnw test -Dtest='dev.krocon.shop.ep014.**'
```

## Dateien
- `app/src/test/java/dev/krocon/shop/ep014/LifecycleTest.java` – Zustände NEW → MANAGED → DETACHED → MANAGED (merge) → REMOVED per `em.contains` bzw. Persistence-Context-Eintrag; merge-Falle (Rückgabewert ignoriert).
- `app/src/main/java/dev/krocon/shop/ep014/CustomerService.java` – `find(id)` (Transaktion endet → detached) und `changeEmail(id, email)` mit `@Transactional` (Lösung).
- `app/src/test/java/dev/krocon/shop/ep014/DetachedChangeTest.java` – echte Commits (`Propagation.NOT_SUPPORTED`): Änderung nach `find` ist weg, `changeEmail` speichert; räumt danach auf.

## Was du sehen solltest
- `persist` → sofort 1 INSERT (IDENTITY braucht die ID); `setName` + `flush` → 1 UPDATE.
- Nach `detach` erzeugt eine Änderung **kein** UPDATE; `merge(c)` liefert `m != c`, `m` ist MANAGED, `c` bleibt DETACHED.
- `remove(m)` → Zustand REMOVED, `em.contains(m)` false, DELETE erst beim `flush`.
- merge-Falle: wer nach `merge` am Original weiterändert, speichert nichts.
- `find(42L)` + `setEmail("neu@shop.de")` → in der DB bleibt die alte Adresse; `changeEmail(42L, …)` schreibt sie.
- Achtung (Hibernate 7.4.1): `persist` → `detach` **ohne** vorheriges `flush` → `merge` wirft bei `Customer`
  (hat die Collection `orders`) eine `NullPointerException` – der Test
  `withoutFlushBeforeDetach_mergeFailsInHibernate741` hält das fest. Mit `em.flush()` vor `detach` läuft alles.
