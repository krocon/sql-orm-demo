# Folge 009 – SQL vs. NoSQL: Wann passt was?   (EN: SQL vs. NoSQL: which fits when?)
Video: „SQL vs. NoSQL: Wann passt was? in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Relational: normalisiert, Joins, Constraints, Transaktionen – flexibel abfragbar. Dokument: eine Bestellung
als ein Objekt, so gespeichert, wie man sie liest. Die Grenzen verschwimmen: PostgreSQL speichert und
durchsucht JSON-Dokumente mit `jsonb`.

## Ausprobieren
```bash
psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/009/modelle.sql
psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/009/jsonb.sql
cat episodes/009/order.json
```

## Dateien
- `modelle.sql` – relationale Abfrage aus dem Video (für Bestellung 1001), eine „andere Frage“ an dieselben Tabellen, Bestellung als JSON zusammengebaut.
- `order.json` – die Bestellung aus dem Video als Dokument (wie in MongoDB).
- `jsonb.sql` – dasselbe Dokument (plus drei weitere) als `jsonb` in `ep009.order_docs`: `->>`, `@>`, `jsonb_array_elements`, GIN-Index, fehlende Integritätsprüfung.

## Was du sehen solltest
- `modelle.sql`: Bestellung 1001 hat 2 Positionen (Produkt 16 × 2, Produkt 49 × 1). Im Shop gehört sie
  „kunde854@shop.example“ – das Dokument im Video (Ana, MUG-1) ist ein vereinfachtes Beispiel.
- `jsonb.sql`: `@> '{"items":[{"sku":"MUG-1"}]}'` findet 1001 und 1002; ausgepackt 5 Positionszeilen;
  Dokument 1004 mit Tippfehler-SKU „MUG1“ wurde klaglos gespeichert – ein Fremdschlüssel hätte es abgelehnt.
