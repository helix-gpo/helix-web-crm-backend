# Todo

## Vor dem Go-Live

### Code und Konfiguration
- ⚠️ Flyway-Baseline erstellen (Schema entsteht aktuell per Hibernate) - DONE!
- ⚠️ AWS-Clients auf die Standard-Credential-Kette umstellen (nicht mehr Profil `helix-crm`) - DONE!
- ⚠️ `/api/public/notifications/test` entfernen - DONE!
- ⚠️ Cron-Jobs mit Zeitzone `Europe/Berlin` - DONE!
- Fail-closed abschließen (Henry anlegen, Übergangs-Ausnahme entfernen)
- Prod-Start: Admin-Rolle, erste Admins, Rechnungsnummern-Startwert
- Environments Frontend und Backend (Prod-Profil, Env-Tabelle, Prod-Cognito-Client)
- CORS für die CRM-Domain
- Health-Endpunkt für den Load Balancer
- Altlasten: `created_by` mit UUIDs, `console.log`, altes Passwort rotieren, N+1-Abfragen
- Frontend Validierungen (Zeichenlänge etc.)
- Bilder zurecht schneiden

### Tests und Doku
- Autorisierungs-Tests (Rollen, Aktionen, Scope) und Domain-Tests
- Frontend-Tests und ein E2E-Smoke
- JavaDoc und Confluence-Seite
- README mit Setup und Env-Tabelle

### E-Mail
- Mail-Versand in eigenen Service auslagern
- Einladungsmail für Mitarbeiter mit Login-Link
- Texte final mit dem Geschäftspartner

### Docker
- Dockerfile Backend, docker-compose lokal
- Prod-Simulation lokal gegen frische Datenbank

### AWS
- Entscheiden: IaC-Werkzeug, Cognito-Region, Hosting Frontend und Website
- Grundlagen: Account, Netzwerk, RDS, ECR
- Secrets Manager, IAM-Rollen, S3 Prod-Bucket, SES, Cognito Prod
- ECS Fargate, Load Balancer, Route 53, Zertifikate
- CRM-Frontend auf S3 + CloudFront, WAF

### Pipeline und Betrieb
- Pipeline für Backend und Frontend
- Logging, Monitoring, Alarme
- Backups mit Restore-Test, Runbooks, Datenschutz

### Go-Live
- Abnahme-Durchlauf (Login bis Rechnung, Mail, PDF, Referenz-Link)
- Alte Website ersetzen

## Danach
- Geschäftliche Postfächer (Google Workspace)
- Website-Chatbot: n8n-AI durch Google-MCP ersetzen
- Mitarbeiter-Seite mit Suche und Filter

## Future
- E-Rechnung (XML)
- Eingangsrechnungen
- Account-Details für Consulting mit Timeline
- Teams für Rollen, Projekte und Mandanten
- Team auf der Website
- VPN (Netzwerk und Firewall)
