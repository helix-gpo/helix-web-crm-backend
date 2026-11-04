# Todo

## Vor dem Go-Live

### 2. Code fertigstellen
- Prod-Start als Migration vorbereiten (Admin-Rolle, erste Admins, Rechnungsnummern-Startwert)

### 3. Tests
- Autorisierungs-Tests (Rollen, Aktionen, Scope) und Domain-Tests
- Frontend-Tests und ein E2E-Smoke

### 4. Docker
- Dockerfile Backend + Website (SSR), docker-compose lokal
- Prod-Simulation lokal gegen frische Datenbank

### 5. AWS
- Grundlagen: Account, Netzwerk, RDS, ECR
- Secrets Manager, IAM-Rollen, S3 Prod-Bucket, SES (Domain verifizieren), Cognito Prod (+ TODO-Werte in CRM environment.prod.ts eintragen)
- ECS Fargate (Backend + Website), Load Balancer, Route 53, Zertifikate
- CRM-Frontend auf S3 + CloudFront, WAF

### 6. Pipeline und Betrieb
- Pipeline für Backend, CRM-Frontend und Website
- Logging, Monitoring, Alarme (inkl. fehlgeschlagener Mail-Events)
- Backups mit Restore-Test, Runbooks, Datenschutz

### 7. Doku
- README mit Setup und Env-Tabelle
- JavaDoc und Confluence-Seite

### 8. Go-Live
- Mail-Texte final mit dem Geschäftspartner
- Prod-Start ausführen
- Abnahme-Durchlauf (Login bis Rechnung, Mail, PDF, Referenz-Link)
- Alte Website ersetzen

## Danach
- Website-Chatbot: n8n-AI durch Google-MCP ersetzen
- Mitarbeiter-Seite mit Suche und Filter

## Future
- Angebotserstellung für Mandanten (am Projekt oder direkt am Mandant) - erstes Feature, mit dem die Pipeline getestet wird
- Rechnungen: Pauschal-Zahlungen monatlich (hängen an einem bestimmten Projekt)
- E-Rechnung (XML)
- Eingangsrechnungen
- Account-Details für Consulting mit Timeline
- Teams für Rollen, Projekte und Mandanten
- Team auf der Website
- VPN (Netzwerk und Firewall) + Verstecken vor der Öffentlichkeit
