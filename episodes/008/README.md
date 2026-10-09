# Folge 008 – Normalisierung: Redundanzen vermeiden   (EN: Database normalization: avoid redundancy)
Video: „Normalisierung: Redundanzen vermeiden in 60 Sekunden“ · Serie SQL, ORM & Hibernate

## Worum es geht
Eine Tabelle für alles erzeugt Tippfehler, Listen in Zellen und Update-Anomalien. Normalisiert steht jede
Information an genau einem Ort, Fremdschlüssel verbinden die Tabellen. Der Preis zum Bestellzeitpunkt
(`order_items.unit_price`) ist dagegen Historie, keine Redundanz.

## Ausprobieren
```bash
for f in alles_in_einer schema historie; do psql -h 127.0.0.1 -p 5433 -U shop -d shop -f episodes/008/$f.sql; done
```

## Dateien
- `alles_in_einer.sql` – die Tabelle aus dem Code-Fenster „Problem“ (Schema `ep008`), Update-Anomalie, Zerlegen der Liste in Zeilen (1NF).
- `schema.sql` – normalisiertes Kurz-Schema (Code-Fenster „Hook“ + products/order_items) in `ep008n`, Umzug = 1 Zeile, FK-Verletzung als DO-Block. Das vollständige Schema aller Folgen ist `schema/shop.sql`.
- `historie.sql` – Preis von MUG-1 um 20 % erhöhen (in Transaktion mit ROLLBACK): alte Positionen behalten ihren Preis.

## Was du sehen solltest
- Flache Tabelle: Ana wohnt in „Hambrug“ und „Hamburg“; nach `UPDATE … WHERE city = 'Hamburg'` in „Berlin“ **und** „Hambrug“.
- Normalisiert: Umzug ändert genau eine Zeile, alle 3 Bestellungen zeigen „Berlin“; Tasse 2×, Shirt 1×, Poster 1×.
  Bestellung für Kunde 99 → `violates foreign key constraint "orders_customer_id_fkey"`.
- Historie: MUG-1 kostet danach 90,70 € statt 75,58 €, die 200 alten Positionen bleiben bei 75,58 €.
  Umsatz MUG-1 historisch ≈ 30.400 €, mit heutigem Preis gerechnet wären es ≈ 36.500 €.
