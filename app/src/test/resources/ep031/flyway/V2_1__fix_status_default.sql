-- Vorwärts korrigieren: In V2 fehlte der Default. NICHT V2 ändern, sondern neue Migration.
ALTER TABLE orders ALTER COLUMN status SET DEFAULT 'NEW';
