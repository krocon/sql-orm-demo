-- Folge 012: die Getter-Kette order.getCustomer().getEmail() als JOIN
-- psql -h 127.0.0.1 -U shop -d shop -f episodes/012/navigation.sql
SELECT c.email
FROM orders o
JOIN customers c ON c.id = o.customer_id
WHERE o.id = 1001;

-- n:m Bestellung ↔ Produkt: nur über die Join-Tabelle order_items
SELECT p.sku, oi.quantity, oi.unit_price
FROM order_items oi
JOIN products p ON p.id = oi.product_id
WHERE oi.order_id = 1001
ORDER BY p.sku;
