# Todo

## Vor dem Go-Live

### 2. AWS
- Grundlagen: Account, Netzwerk, RDS, ECR
- Secrets Manager, IAM-Rollen, S3 Prod-Bucket, SES (Domain verifizieren), Cognito Prod (+ TODO-Werte in CRM environment.prod.ts eintragen)
- ECS Fargate (Backend + Website), Load Balancer, Route 53, Zertifikate
- CRM-Frontend auf S3 + CloudFront, WAF

### 3. Pipeline und Betrieb
- Pipeline für Backend, CRM-Frontend und Website
- Logging, Monitoring, Alarme (inkl. fehlgeschlagener Mail-Events)
- Backups mit Restore-Test, Runbooks, Datenschutz

### 4. Doku
- README mit Setup und Env-Tabelle
- JavaDoc und Confluence-Seite

### 5. Go-Live
- Mail-Texte final mit dem Geschäftspartner
- Prod-Start ausführen (Bootstrap-Admins Felix + Henry)
- Abnahme-Durchlauf (Login bis Rechnung, Mail, PDF, Referenz-Link)
- Alte Website ersetzen

## Danach
- Website-Chatbot: n8n-AI durch Google-MCP ersetzen

## Future
- Mitarbeiter-Seite mit Suche und Filter
- Angebotserstellung für Mandanten (am Projekt oder direkt am Mandant) - erstes Feature, mit dem die Pipeline getestet wird
- Dokumentenspeicherung von Mandanten
- Rechnungen: Pauschal-Zahlungen monatlich (hängen an einem bestimmten Projekt)
- E-Rechnung (XML)
- Eingangsrechnungen
- Account-Details für Consulting mit Timeline
- Teams für Rollen, Projekte und Mandanten
- Team auf der Website
- VPN (Netzwerk und Firewall) + Verstecken vor der Öffentlichkeit
