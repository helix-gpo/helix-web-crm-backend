-- =====================================================
-- MOCK-DATEN für Helix CRM (lokale Entwicklung)
-- Reihenfolge beachten wegen Foreign-Key-Abhängigkeiten:
-- tenants -> tenant_partners -> projects -> milestones
-- -> invoices -> invoice_line_items -> invoice_sequences
-- -> testimonial_invitations -> testimonials
-- =====================================================

-- =====================================================
-- TENANTS
-- =====================================================

INSERT INTO tenants (
    id, company_name, legal_name, vat_id,
    street, house_number, postal_code, city, country_code,
    contact_email, contact_phone, status, visible_on_website,
    created_at, updated_at, version
) VALUES
      (UUID_TO_BIN('10000000-0000-0000-0000-000000000001'),
       'Michael Breuer Steuerberatung', 'Michael Breuer Steuerberatung', 'DE123456789',
       'Kanzleistraße', '12', '41460', 'Neuss', 'DE',
       'kanzlei@breuer-steuerberatung.de', '+49 2131 1234560', 'ACTIVE', TRUE,
       '2025-10-15 09:00:00', '2025-10-15 09:00:00', 0),

      (UUID_TO_BIN('10000000-0000-0000-0000-000000000002'),
       'Geschwister Weiß-Stiftung', 'Geschwister Weiß-Stiftung', NULL,
       'Stiftungsallee', '3', '41460', 'Neuss', 'DE',
       'info@geschwister-weiss-stiftung.de', '+49 2131 9876540', 'ACTIVE', TRUE,
       '2025-08-20 09:00:00', '2025-08-20 09:00:00', 0),

      (UUID_TO_BIN('10000000-0000-0000-0000-000000000003'),
       'Klein Handwerksbetrieb', NULL, NULL,
       'Handwerkerweg', '7', '40210', 'Düsseldorf', 'DE',
       'info@klein-handwerk.de', '+49 211 1122330', 'PROSPECT', FALSE,
       '2026-01-05 09:00:00', '2026-01-05 09:00:00', 0),

      (UUID_TO_BIN('10000000-0000-0000-0000-000000000004'),
       'Fischer Vertrieb GmbH', 'Fischer Vertrieb GmbH', 'DE987654321',
       'Vertriebsring', '21', '50667', 'Köln', 'DE',
       'kontakt@fischer-vertrieb.de', '+49 221 5544330', 'INACTIVE', FALSE,
       '2025-05-10 09:00:00', '2025-11-01 09:00:00', 0);

-- =====================================================
-- TENANT_PARTNERS
-- =====================================================

INSERT INTO tenant_partners (
    id, tenant_id, first_name, last_name, role, email, phone,
    created_at, updated_at, version
) VALUES
      (UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), UUID_TO_BIN('10000000-0000-0000-0000-000000000001'),
       'Michael', 'Breuer', 'Steuerberater', 'm.breuer@breuer-steuerberatung.de', '+49 2131 1234561',
       '2025-10-15 09:00:00', '2025-10-15 09:00:00', 0),

      (UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'),
       'Susanne', 'Weiß', 'Vorstand', 's.weiss@geschwister-weiss-stiftung.de', '+49 2131 9876541',
       '2025-08-20 09:00:00', '2025-08-20 09:00:00', 0),

      (UUID_TO_BIN('20000000-0000-0000-0000-000000000003'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'),
       'Thomas', 'Klein', 'Geschäftsführer', 't.klein@klein-handwerk.de', '+49 211 1122331',
       '2026-01-05 09:00:00', '2026-01-05 09:00:00', 0),

      (UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), UUID_TO_BIN('10000000-0000-0000-0000-000000000004'),
       'Julia', 'Fischer', 'Teamleitung Vertrieb', 'j.fischer@fischer-vertrieb.de', '+49 221 5544331',
       '2025-05-10 09:00:00', '2025-05-10 09:00:00', 0);

-- =====================================================
-- PROJECTS
-- =====================================================

INSERT INTO projects (
    id, tenant_id, title, description, full_description,
    status, start_date, end_date, visible_on_website,
    created_at, updated_at, version
) VALUES
      (UUID_TO_BIN('30000000-0000-0000-0000-000000000001'), UUID_TO_BIN('10000000-0000-0000-0000-000000000001'),
       'Digitale Kanzlei-Präsenz',
       'Neuaufbau der Online-Sichtbarkeit für eine Steuerkanzlei in Neuss.',
       'Für eine etablierte Steuerkanzlei in Neuss haben wir die komplette digitale Präsenz überarbeitet, inklusive technischer SEO-Analyse und Google-Business-Profil-Optimierung.',
       'COMPLETED', '2025-11-01', '2025-11-30', TRUE,
       '2025-11-01 09:00:00', '2025-11-30 09:00:00', 0),

      (UUID_TO_BIN('30000000-0000-0000-0000-000000000002'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'),
       'Stiftungs-Website',
       'Vollständig neu gestaltete Website für eine gemeinnützige Stiftung.',
       'Für eine gemeinnützige Stiftung haben wir eine komplett neue Website als Angular-Projekt konzipiert und umgesetzt, mit eigenem Design-System und Spendenbereich.',
       'IN_PROGRESS', '2025-09-01', NULL, TRUE,
       '2025-09-01 09:00:00', '2026-01-10 09:00:00', 0),

      (UUID_TO_BIN('30000000-0000-0000-0000-000000000003'), UUID_TO_BIN('10000000-0000-0000-0000-000000000004'),
       'Prozessautomatisierung Vertrieb',
       'Automatisierung wiederkehrender Vertriebsprozesse mithilfe von n8n-Workflows.',
       'Ein Vertriebsteam verbrachte täglich mehrere Stunden mit manuellen Abläufen. Wir haben die Prozesse analysiert und automatisiert.',
       'COMPLETED', '2025-06-01', '2025-07-15', TRUE,
       '2025-06-01 09:00:00', '2025-07-15 09:00:00', 0),

      (UUID_TO_BIN('30000000-0000-0000-0000-000000000004'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'),
       'Interne Prozess-Standardisierung',
       'Einführung einheitlicher, dokumentierter Abläufe für ein mittelständisches Unternehmen.',
       'Uneinheitliche Arbeitsabläufe erschwerten die Einarbeitung. Wir haben die Kernprozesse dokumentiert und standardisiert.',
       'LEAD', '2026-02-01', NULL, FALSE,
       '2026-01-20 09:00:00', '2026-01-20 09:00:00', 0);

-- =====================================================
-- PROJECT_HIGHLIGHTS
-- =====================================================

INSERT INTO project_highlights (project_id, sort_order, highlight) VALUES
                                                                       (UUID_TO_BIN('30000000-0000-0000-0000-000000000001'), 0, 'Technische SEO-Analyse & Fehlerbehebung'),
                                                                       (UUID_TO_BIN('30000000-0000-0000-0000-000000000001'), 1, 'Google-Business-Profil-Optimierung'),
                                                                       (UUID_TO_BIN('30000000-0000-0000-0000-000000000001'), 2, 'Laufende Betreuung der digitalen Sichtbarkeit'),

                                                                       (UUID_TO_BIN('30000000-0000-0000-0000-000000000002'), 0, 'Individuelles Angular-Frontend mit Mixin-System'),
                                                                       (UUID_TO_BIN('30000000-0000-0000-0000-000000000002'), 1, 'Vorstands- & Kuratoriumsseite mit Chronik'),
                                                                       (UUID_TO_BIN('30000000-0000-0000-0000-000000000002'), 2, 'Dokumenten-Download & Kontaktbereich'),

                                                                       (UUID_TO_BIN('30000000-0000-0000-0000-000000000003'), 0, 'Workflow-Automatisierung mit n8n'),
                                                                       (UUID_TO_BIN('30000000-0000-0000-0000-000000000003'), 1, 'KI-gestützte Datenverarbeitung'),

                                                                       (UUID_TO_BIN('30000000-0000-0000-0000-000000000004'), 0, 'Prozessdokumentation & Standardisierung');

-- =====================================================
-- PROJECT_TAGS
-- =====================================================

INSERT INTO project_tags (project_id, tag_value, tag_color) VALUES
                                                                (UUID_TO_BIN('30000000-0000-0000-0000-000000000001'), 'SEO', '#03dbff'),
                                                                (UUID_TO_BIN('30000000-0000-0000-0000-000000000001'), 'Google Business', '#fc03d5'),

                                                                (UUID_TO_BIN('30000000-0000-0000-0000-000000000002'), 'Angular', '#0d1424'),
                                                                (UUID_TO_BIN('30000000-0000-0000-0000-000000000002'), 'Webdesign', '#03dbff'),

                                                                (UUID_TO_BIN('30000000-0000-0000-0000-000000000003'), 'Automatisierung', '#fc03d5'),
                                                                (UUID_TO_BIN('30000000-0000-0000-0000-000000000003'), 'KI-Integration', '#03dbff'),

                                                                (UUID_TO_BIN('30000000-0000-0000-0000-000000000004'), 'Prozessoptimierung', '#0d1424');

-- =====================================================
-- MILESTONES (mit Preis, damit sie sich als Rechnungsposition eignen)
-- =====================================================

INSERT INTO milestones (
    id, project_id, title, description, due_date, status,
    price_amount, price_currency_code,
    created_at, updated_at, version
) VALUES
      (UUID_TO_BIN('40000000-0000-0000-0000-000000000001'), UUID_TO_BIN('30000000-0000-0000-0000-000000000001'),
       'SEO-Analyse & Fehlerbehebung', 'Technische Prüfung und Behebung des Indexierungsfehlers.',
       '2025-11-10', 'DONE', 850.00, 'EUR', '2025-11-01 09:00:00', '2025-11-10 09:00:00', 0),

      (UUID_TO_BIN('40000000-0000-0000-0000-000000000002'), UUID_TO_BIN('30000000-0000-0000-0000-000000000001'),
       'Google-Business-Profil-Optimierung', 'Vollständige Überarbeitung des Profils.',
       '2025-11-28', 'DONE', 450.00, 'EUR', '2025-11-10 09:00:00', '2025-11-28 09:00:00', 0),

      (UUID_TO_BIN('40000000-0000-0000-0000-000000000003'), UUID_TO_BIN('30000000-0000-0000-0000-000000000002'),
       'Konzept & Design-System', 'Erarbeitung des visuellen Erscheinungsbilds.',
       '2025-09-30', 'DONE', 1800.00, 'EUR', '2025-09-01 09:00:00', '2025-09-30 09:00:00', 0),

      (UUID_TO_BIN('40000000-0000-0000-0000-000000000004'), UUID_TO_BIN('30000000-0000-0000-0000-000000000002'),
       'Umsetzung & Launch', 'Technische Umsetzung und Go-Live der Website.',
       '2026-02-15', 'IN_PROGRESS', 2400.00, 'EUR', '2025-10-01 09:00:00', '2025-10-01 09:00:00', 0);

-- =====================================================
-- INVOICES (eine ausgestellte Beispielrechnung)
-- =====================================================

INSERT INTO invoices (
    id, invoice_number, tenant_id, project_id, status, currency_code,
    issue_date, due_date, payment_terms_days, buyer_reference,
    seller_name, seller_vat_id, seller_email, seller_iban, seller_bic,
    seller_street, seller_house_number, seller_postal_code, seller_city, seller_country_code,
    buyer_name, buyer_vat_id, buyer_email, buyer_iban, buyer_bic,
    buyer_street, buyer_house_number, buyer_postal_code, buyer_city, buyer_country_code,
    created_at, updated_at, version
) VALUES
    (UUID_TO_BIN('50000000-0000-0000-0000-000000000001'), 'RE-2025-00001',
     UUID_TO_BIN('10000000-0000-0000-0000-000000000001'), UUID_TO_BIN('30000000-0000-0000-0000-000000000001'),
     'ISSUED', 'EUR', '2025-12-01', '2025-12-15', 14, NULL,
     'Helix GPO', 'DE111222333', 'helix.gpo@gmail.com', 'DE00 0000 0000 0000 0000 00', 'COBADEFFXXX',
     'Erftstraße', '50', '41460', 'Neuss', 'DE',
     'Michael Breuer Steuerberatung', 'DE123456789', 'kanzlei@breuer-steuerberatung.de', NULL, NULL,
     'Kanzleistraße', '12', '41460', 'Neuss', 'DE',
     '2025-12-01 10:00:00', '2025-12-01 10:00:00', 0);

-- =====================================================
-- INVOICE_LINE_ITEMS (zwei Meilensteine als Positionen übernommen)
-- =====================================================

INSERT INTO invoice_line_items (
    id, invoice_id, position_number, source, milestone_id, description,
    quantity, unit_code, unit_price_amount, unit_price_currency, tax_rate_percentage,
    created_at, updated_at, version
) VALUES
      (UUID_TO_BIN('51000000-0000-0000-0000-000000000001'), UUID_TO_BIN('50000000-0000-0000-0000-000000000001'),
       1, 'MILESTONE', UUID_TO_BIN('40000000-0000-0000-0000-000000000001'), 'SEO-Analyse & Fehlerbehebung',
       1.00, 'C62', 850.00, 'EUR', 19.00, '2025-12-01 10:00:00', '2025-12-01 10:00:00', 0),

      (UUID_TO_BIN('51000000-0000-0000-0000-000000000002'), UUID_TO_BIN('50000000-0000-0000-0000-000000000001'),
       2, 'MILESTONE', UUID_TO_BIN('40000000-0000-0000-0000-000000000002'), 'Google-Business-Profil-Optimierung',
       1.00, 'C62', 450.00, 'EUR', 19.00, '2025-12-01 10:00:00', '2025-12-01 10:00:00', 0);

-- =====================================================
-- INVOICE_SEQUENCES (Zähler auf dem Stand der Beispielrechnung)
-- =====================================================

INSERT INTO invoice_sequences (sequence_year, `last_value`) VALUES
    (2025, 1);

-- =====================================================
-- TESTIMONIAL_INVITATIONS
-- Hinweis: token_hash sind hier nur Platzhalter (keine echten SHA-256-Hashes) -
-- über diese Mock-Einträge kann man sich nicht per echtem Token einloggen,
-- sie dienen nur der CRM-Ansicht/Liste
-- =====================================================

INSERT INTO testimonial_invitations (
    id, tenant_id, partner_id, project_id, token_hash, status,
    expires_at, used_at, created_at, updated_at, version
) VALUES
      (UUID_TO_BIN('60000000-0000-0000-0000-000000000001'), UUID_TO_BIN('10000000-0000-0000-0000-000000000001'),
       UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), UUID_TO_BIN('30000000-0000-0000-0000-000000000001'),
       'mock-hash-breuer-001', 'USED',
       '2026-01-01 00:00:00', '2025-12-05 14:00:00', '2025-12-01 10:00:00', '2025-12-05 14:00:00', 0),

      (UUID_TO_BIN('60000000-0000-0000-0000-000000000002'), UUID_TO_BIN('10000000-0000-0000-0000-000000000002'),
       UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), UUID_TO_BIN('30000000-0000-0000-0000-000000000002'),
       'mock-hash-weiss-002', 'USED',
       '2026-02-01 00:00:00', '2025-10-10 11:30:00', '2025-09-15 10:00:00', '2025-10-10 11:30:00', 0),

      (UUID_TO_BIN('60000000-0000-0000-0000-000000000003'), UUID_TO_BIN('10000000-0000-0000-0000-000000000003'),
       UUID_TO_BIN('20000000-0000-0000-0000-000000000003'), NULL,
       'mock-hash-klein-003', 'PENDING',
       '2026-03-01 00:00:00', NULL, '2026-01-25 09:00:00', '2026-01-25 09:00:00', 0),

      (UUID_TO_BIN('60000000-0000-0000-0000-000000000004'), UUID_TO_BIN('10000000-0000-0000-0000-000000000004'),
       UUID_TO_BIN('20000000-0000-0000-0000-000000000004'), UUID_TO_BIN('30000000-0000-0000-0000-000000000003'),
       'mock-hash-fischer-004', 'USED',
       '2025-08-01 00:00:00', '2025-06-20 16:00:00', '2025-06-15 10:00:00', '2025-06-20 16:00:00', 0);

-- =====================================================
-- TESTIMONIALS
-- =====================================================

INSERT INTO testimonials (
    id, invitation_id, tenant_id, partner_id, project_id,
    partner_name_snapshot, partner_role_snapshot, company_name_snapshot,
    description, rating, status, visible_on_website,
    created_at, updated_at, version
) VALUES
      (UUID_TO_BIN('70000000-0000-0000-0000-000000000001'), UUID_TO_BIN('60000000-0000-0000-0000-000000000001'),
       UUID_TO_BIN('10000000-0000-0000-0000-000000000001'), UUID_TO_BIN('20000000-0000-0000-0000-000000000001'),
       UUID_TO_BIN('30000000-0000-0000-0000-000000000001'),
       'Michael Breuer', 'Steuerberater', 'Michael Breuer Steuerberatung',
       'Die Zusammenarbeit mit Helix GPO war von Anfang an unkompliziert und zielgerichtet. Unsere Prozesse laufen jetzt spürbar effizienter.',
       5, 'APPROVED', TRUE, '2025-12-05 14:00:00', '2025-12-06 09:00:00', 0),

      (UUID_TO_BIN('70000000-0000-0000-0000-000000000002'), UUID_TO_BIN('60000000-0000-0000-0000-000000000002'),
       UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), UUID_TO_BIN('20000000-0000-0000-0000-000000000002'),
       UUID_TO_BIN('30000000-0000-0000-0000-000000000002'),
       'Susanne Weiß', 'Vorstand', 'Geschwister Weiß-Stiftung',
       'Sehr professionelle Beratung mit echtem Verständnis für unsere Branche. Die neue Website hat unsere Sichtbarkeit deutlich verbessert.',
       5, 'APPROVED', TRUE, '2025-10-10 11:30:00', '2025-10-11 09:00:00', 0),

      (UUID_TO_BIN('70000000-0000-0000-0000-000000000003'), UUID_TO_BIN('60000000-0000-0000-0000-000000000004'),
       UUID_TO_BIN('10000000-0000-0000-0000-000000000004'), UUID_TO_BIN('20000000-0000-0000-0000-000000000004'),
       UUID_TO_BIN('30000000-0000-0000-0000-000000000003'),
       'Julia Fischer', 'Teamleitung Vertrieb', 'Fischer Vertrieb GmbH',
       'Die Automatisierung unserer internen Abläufe hat uns enorm viel Zeit gespart. Klare Empfehlung für jedes wachsende Unternehmen.',
       4, 'PENDING_REVIEW', FALSE, '2025-06-20 16:00:00', '2025-06-20 16:00:00', 0);
