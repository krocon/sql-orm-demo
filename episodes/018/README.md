# Folge 018 – First- & Second-Level-Cache   (EN: First- & second-level cache)
Video: „First- & Second-Level-Cache in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Der First-Level-Cache ist der Persistence Context: immer an, aber nur eine Transaktion lang – jeder neue
Request selektiert wieder. Der Second-Level-Cache lebt über Sessions hinweg, ist optional und lohnt für oft
gelesene, selten geänderte Daten. Ändert jemand die Tabelle an Hibernate vorbei, liefert er veraltete Daten.

## Ausprobieren
```bash
cd app
./mvnw test -Dtest='dev.krocon.shop.ep018.**'     # PostgreSQL 18 per Testcontainers (Docker nötig)

# alternativ gegen die Compose-Datenbank (Flyway legt Schema + Testdaten in shop_app an):
docker compose up -d                             # im Repo-Wurzelverzeichnis, .env aus .env.example
DB_URL=jdbc:postgresql://127.0.0.1:5433/shop_app POSTGRES_USER=shop POSTGRES_PASSWORD=change-me \
  ./mvnw test -Dtest='dev.krocon.shop.ep018.**'
```

## Dateien
- `app/src/main/java/dev/krocon/shop/ep018/ProductCatalogService.java` – `show(id)` aus dem Video (2× `find`, 1 SELECT) und `showCached(id)`.
- `app/src/main/java/dev/krocon/shop/ep018/CachedProduct.java` – Entity auf `products` mit `@Cacheable` + `@Cache(usage = READ_WRITE)`.
- `app/src/test/java/dev/krocon/shop/ep018/FirstLevelCacheTest.java` – zweites `find` ohne Query; jeder neue Request (Transaktion) selektiert wieder.
- `app/src/test/java/dev/krocon/shop/ep018/SecondLevelCacheTest.java` – Cache-Hit über Transaktionen, Query Cache, veraltete Daten nach UPDATE per `JdbcClient` (räumt danach auf).
- `app/src/test/java/dev/krocon/shop/ep018/MapRegionFactory.java` – Mini-Cache-Provider auf `ConcurrentHashMap`, **nur für die Demo** (keine zusätzliche Dependency, kein Ablauf, nicht clusterfähig).

## Second-Level-Cache produktiv: JCache + Ehcache
Der Test nutzt den Demo-Provider. In echten Projekten bindest du einen JCache-Provider an
(so auch mit den Tests dieser Folge ausprobiert – grün, gleiche Zahlen):
```xml
<dependency>
  <groupId>org.hibernate.orm</groupId>
  <artifactId>hibernate-jcache</artifactId>        <!-- Version über Spring Boot -->
</dependency>
<dependency>
  <groupId>org.ehcache</groupId>
  <artifactId>ehcache</artifactId>
  <classifier>jakarta</classifier>                  <!-- Version über Spring Boot (3.12.0) -->
</dependency>
```
```yaml
spring.jpa.properties.hibernate.cache:
  use_second_level_cache: true
  region.factory_class: jcache
  use_query_cache: true          # nur bei Bedarf
```
Ohne eigene `ehcache.xml` legt Hibernate die Regionen mit Default-Einstellungen an und warnt (`HHH90001006`).
Achtung: Liegt `hibernate-jcache` auf dem Classpath, ist der Second-Level-Cache für `@Cacheable`-Entities
auch ohne `use_second_level_cache: true` aktiv. Im Cluster brauchst du einen verteilten Cache.

## Was du sehen solltest
- First-Level: `show(1L)` → 2× `find`, **1 SELECT**, dasselbe Objekt. Drei Requests nacheinander → **3 SELECTs**.
- Second-Level: `showCached(1L)` dreimal → **1 SELECT**, 1 Cache-Put, 2 Cache-Hits.
- Query Cache: dieselbe Query mit Hint `org.hibernate.cacheable` zweimal → 1 SELECT, 1 Query-Cache-Hit.
- Invalidierung: `UPDATE products SET price = price + 10` per JDBC → `showCached` liefert weiter den **alten Preis**;
  erst `emf.getCache().evict(CachedProduct.class, 1L)` holt den neuen.
