-- tax number (Steuernummer) next to the VAT id - both are separate fields in an e-invoice too
ALTER TABLE invoices
    ADD COLUMN seller_tax_number VARCHAR(20) NULL AFTER seller_vat_id,
    ADD COLUMN buyer_tax_number  VARCHAR(20) NULL AFTER buyer_vat_id;