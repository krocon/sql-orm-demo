-- Folge 012: Vererbung – eine Tabelle für die ganze Klassenhierarchie (SINGLE_TABLE)
CREATE TABLE ep012_payments (
  id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  type       varchar(31) NOT NULL,      -- Diskriminator: CARD | INVOICE
  amount     numeric(10,2) NOT NULL,
  card_last4 text,                      -- nur CardPayment
  due_days   integer                    -- nur InvoicePayment
);
