-- Folge 031: Expand-Schritt. Neue Spalte NEBEN der alten, nullable, Daten kopieren.
-- Alter Code liest/schreibt weiter "phone", neuer Code nutzt "phone_e164".
-- Contract (DROP COLUMN phone) kommt erst in einer SPÄTEREN Migration,
-- wenn kein laufender Code "phone" mehr nutzt.
ALTER TABLE ep031_customer_contacts ADD COLUMN phone_e164 text;

UPDATE ep031_customer_contacts
SET    phone_e164 = '+49' || regexp_replace(substr(phone, 2), '\D', '', 'g')
WHERE  phone LIKE '0%' AND phone_e164 IS NULL;
