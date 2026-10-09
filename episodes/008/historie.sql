-- Folge 008 – Historie ≠ Redundanz: Preisänderung ändert alte Bestellungen nicht
-- Läuft auf den shop-Tabellen, alles in einer Transaktion mit ROLLBACK.
BEGIN;

-- Tasse (MUG-1): aktueller Preis und Preis in den bisherigen Bestellpositionen
SELECT p.sku, p.price AS preis_heute,
       min(i.unit_price) AS preis_in_bestellungen, count(*) AS positionen
FROM products p JOIN order_items i ON i.product_id = p.id
WHERE p.sku = 'MUG-1'
GROUP BY p.sku, p.price;

-- Preiserhöhung um 20 %
UPDATE products SET price = round(price * 1.2, 2) WHERE sku = 'MUG-1';

-- Alte Positionen und Bestellsummen bleiben unverändert – sie haben ihren eigenen unit_price
SELECT p.sku, p.price AS preis_heute,
       min(i.unit_price) AS preis_in_bestellungen, count(*) AS positionen
FROM products p JOIN order_items i ON i.product_id = p.id
WHERE p.sku = 'MUG-1'
GROUP BY p.sku, p.price;

-- Gegenbeispiel: OHNE unit_price müsste man den Bestellwert aus dem heutigen Preis rechnen
-- → alte Rechnungen wären plötzlich teurer
SELECT round(sum(i.quantity * i.unit_price), 2) AS umsatz_historisch,
       round(sum(i.quantity * p.price), 2)      AS umsatz_mit_heutigem_preis
FROM order_items i JOIN products p ON p.id = i.product_id
WHERE p.sku = 'MUG-1';

ROLLBACK;
