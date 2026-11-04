# Todo

## Vor dem Go-Live

### 1. Entscheidungen - DONE!
- IaC: Terraform · Cognito Prod: eu-central-1 (Dev bleibt us-east-1)
- Domains: www.helix-gpo.com, api.helix-gpo.com, crm.helix-gpo.com (Route 53 vorhanden)
- Hosting: Backend + Website auf ECS Fargate hinter einem ALB, CRM-Frontend auf S3 + CloudFront
- Mails: SES mit noreply@helix-gpo.com, Reply-To vorerst Gmail · kein Staging

### 2. Code fertigstellen
- ⚠️ Flyway-Baseline, Credential-Kette, Test-Endpunkt entfernt, Cron-Zeitzone - DONE!
- Fail-closed abschließen - DONE!
- Altlasten: `created_by`, `console.log`, N+1 - DONE!
- Testrunde: Partner beim Mandanten, Mandant → Projekt, Mail-Button, Mandanten-Filter - DONE!
- Mail-Versand: Templates, typisierte Mails, Versand nach Commit mit Retry, Login-Link - DONE!
- Validierungen: Backend-Längen, Frontend maxlength + Zähler, E-Mail-Prüfung, doppelte Toasts - DONE!
- Health-Endpunkt (Backend /actuator/health/liveness, Website /healthz) - DONE!
- Environments
  - Backend: Prod-Profil, CORS aus Config, Fail-fast ohne Defaults - DONE!
  - CRM-Frontend: environment.prod.ts, Prod-Cognito, Build-Konfiguration
- Website an Backend anbinden (Projekte, Referenzen, Feedback-Submit inkl. Validierung, Env-URLs auf /api/public)
  - öffentlicher Bild-Endpunkt (Projektbilder + Referenzfoto mit Einwilligung)
  - /healthz testen
- Prod-Start als Migration vorbereiten (Admin-Rolle, erste Admins, Rechnungsnummern-Startwert)
- Bilder zurecht schneiden

### 3. Tests
- Autorisierungs-Tests (Rollen, Aktionen, Scope) und Domain-Tests
- Frontend-Tests und ein E2E-Smoke

### 4. Docker
- Dockerfile Backend + Website (SSR), docker-compose lokal
- Prod-Simulation lokal gegen frische Datenbank

### 5. AWS
- Grundlagen: Account, Netzwerk, RDS, ECR
- Secrets Manager, IAM-Rollen, S3 Prod-Bucket, SES (Domain verifizieren), Cognito Prod
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
- E-Rechnung (XML)
- Eingangsrechnungen
- Account-Details für Consulting mit Timeline
- Teams für Rollen, Projekte und Mandanten
- Team auf der Website
- VPN (Netzwerk und Firewall) + Verstecken vor der Öffentlichkeit
