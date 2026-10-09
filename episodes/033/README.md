# Folge 033 – Pagination & Sorting            (EN: Pagination & Sorting)
Video: „Pagination & Sorting in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
`findAll()` lädt jede Zeile, obwohl der Admin 20 sieht. `Pageable` mit stabiler Sortierung (id als Tiebreaker) lädt nur eine Seite; `Page` zählt per COUNT, `Slice` nicht. Für tiefe Seiten ist Keyset (`WHERE id > :lastId`) schneller als OFFSET – Spring Data bietet dafür `Window`/`ScrollPosition`. Und JOIN FETCH auf Collections + Pagination ist heikel.

## Ausprobieren
```bash
cd app
./mvnw test -Dtest='dev.krocon.shop.ep033.**'
```

## Dateien
- `app/src/main/java/dev/krocon/shop/ep033/Ep033OrderRepository.java` – `findByStatus(…, Pageable)` (Page), `findSliceByStatus` (Slice), `findFirst20ByOrderByIdAsc(ScrollPosition)` (Window), JOIN-FETCH-Varianten, `findIds` + `findWithItemsByIdIn`
- `app/src/test/java/dev/krocon/shop/ep033/FindAllTest.java` – 5000 Entities vs. 20 pro Seite
- `app/src/test/java/dev/krocon/shop/ep033/PageVsSliceTest.java` – COUNT nur bei Page
- `app/src/test/java/dev/krocon/shop/ep033/KeysetScrollTest.java` – Window/ScrollPosition, Bind-Parameter, EXPLAIN OFFSET vs. Keyset
- `app/src/test/java/dev/krocon/shop/ep033/FetchPaginationTest.java` – JOIN FETCH + Pageable, HHH90003004, Fix „erst IDs, dann Items“

## Was du sehen solltest
- `findAll()`: 5000 Entities geladen. `PageRequest.of(0, 20, Sort.by("createdAt", "id"))`: 20 Entities, SQL `… order by o1_0.created_at,o1_0.id fetch first ? rows only`.
- Page (Seite 2): Daten-Query `… offset ? rows fetch first ? rows only` **plus** `select count(*) from orders o1_0 where o1_0.status=?`. Slice: nur eine Query, holt 21 Zeilen für `hasNext`.
- Keyset: erste Seite `… order by o1_0.id fetch first ? rows only`, nächste `… where o1_0.id>? order by o1_0.id fetch first ? rows only` mit Bind-Parametern `[20]` und `[21]` (Spring Data holt eine Zeile mehr).
- EXPLAIN ANALYZE: `OFFSET 4000 LIMIT 20` → Index Scan liefert **4020** Zeilen (4000 weggeworfen); `WHERE id > 4000 … LIMIT 20` → **20** Zeilen.
- JOIN FETCH + Pageable (Hibernate 7.4.1 + PostgreSQL): Die Video-Query `select o from Order o join fetch o.items` wird **in der DB** paginiert – Hibernate schiebt LIMIT/OFFSET in eine Unterabfrage (`from (select … fetch first ? rows only) o1_0 join order_items …`), keine Warnung. Frühere Versionen (z. B. Hibernate 6) paginieren hier im Speicher.
- Kann Hibernate das nicht umschreiben (hier: `order by i.quantity desc` auf die Collection), kommt `HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory` – 15 000 Entities (alle Orders + Items) für 20 Orders.
- Vorsicht, gemessen mit 7.4.1: Filtert man zusätzlich auf den Fetch-Alias (`join fetch o.items i where i.quantity > 1`) + Pagination, erzeugt Hibernate ungültiges SQL (`missing FROM-clause entry for table "i1_0"`).
- Fix, der überall funktioniert: `select o.id … fetch first ? rows only`, dann `join fetch … where o.id in (…)` – 2 Statements, 60 Entities.
