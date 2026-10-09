-- Folge 004 – Umsatz pro Land (Code-Fenster „Hook“)
-- WHERE filtert Zeilen vor dem Gruppieren, HAVING ganze Gruppen danach.
SELECT c.country, COUNT(*) AS order_count, SUM(o.total) AS revenue
FROM orders o
JOIN customers c ON c.id = o.customer_id
GROUP BY c.country
HAVING SUM(o.total) > 10000
ORDER BY revenue DESC;
-- Mit den Demo-Daten liegen alle 5 Länder über 10.000 € (DE ≈ 442.000 €).

-- HAVING mit spürbarer Wirkung: nur Gruppen über 200.000 € → nur DE
SELECT c.country, SUM(o.total) AS revenue
FROM orders o
JOIN customers c ON c.id = o.customer_id
GROUP BY c.country
HAVING SUM(o.total) > 200000;

-- WHERE + HAVING kombiniert: erst nur bezahlte Bestellungen, dann Gruppen filtern
SELECT c.country, COUNT(*) AS paid_orders, SUM(o.total) AS revenue
FROM orders o
JOIN customers c ON c.id = o.customer_id
WHERE o.status = 'PAID'
GROUP BY c.country
HAVING COUNT(*) > 300
ORDER BY revenue DESC;

-- Die fünf Aggregatfunktionen
SELECT COUNT(*) AS anzahl, SUM(total) AS summe, round(AVG(total), 2) AS schnitt,
       MIN(total) AS kleinste, MAX(total) AS groesste
FROM orders;
