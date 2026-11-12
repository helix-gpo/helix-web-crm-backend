# Todo

## Vor dem Go-Live

### 2. AWS
- ~~Grundlagen: SSO-Zugang, Terraform-State, Netzwerk, Security Groups, RDS, ECR~~
- ~~Secrets Manager, IAM-Rollen, S3 Prod-Bucket, SES (übernommen), Cognito Prod (+ Werte in CRM environment.prod.ts)~~
- ~~Bastion testen: Tunnel aufbauen, App-User `helix_app` per SQL anlegen~~
- ~~Zertifikate, Load Balancer (alte API bleibt bis zum Go-Live aktiv)~~
- ~~ECS Fargate (Backend + Website), erstes Image manuell nach ECR, Admin-Bootstrap (Felix + Helix GPO)~~
- ~~CRM-Frontend auf S3 + CloudFront (crm.helix-gpo.com), Cognito Managed Login mit Branding + Deutsch, WAF~~
- Lokal auf MySQL 8.4 umstellen (docker-compose + Testcontainers), passend zu RDS

### 3. Betrieb (Minimum vor Go-Live)
- Alarme: Backend/Website down (Target Group unhealthy), 5xx-Fehler, RDS CPU/Speicher, an Mail
- Backup-Restore einmal testen (RDS Point-in-Time-Restore in eine Test-Instanz, danach löschen)

### 4. Go-Live
- USt-IdNr. klären (BZSt-Brief / Steuerberater) und in Secret helix-prod/company eintragen
- Mail-Texte final mit dem Geschäftspartner
- Abnahme-Durchlauf vor der Umstellung (per hosts-Datei): Login bis Rechnung
- DNS umstellen: api.helix-gpo.com, www.helix-gpo.com und helix-gpo.com auf den neuen Load Balancer
- Abnahme nach der Umstellung: Henry als Mitarbeiter einladen, Referenz-Link (Mail -> /feedback -> Abschicken), Rechnung ausstellen + per Mail mit PDF (erste echte Nummer RE-2026-00001)
- hosts-Datei-Einträge entfernen, alte Website ist damit ersetzt

## Danach

### Aufräumen (1-2 Wochen nach Go-Live)
- Alte Infrastruktur abbauen: Staging-ALB + altes Backend, alte Staging-RDS, alte CloudFront/S3-Website, alter n8n-Zertifikatseintrag - Stiftungs-Seiten nicht anfassen
- IAM-User AWS-CLI-ECR: Access-Keys löschen (Images kommen dann aus der Pipeline)
- DMARC von p=none auf p=quarantine verschärfen
- Optional: VPN (WireGuard + WAF-IP-Sperre für CRM und /api/crm)

### Pipeline und Betrieb
- Pipeline für Backend, CRM-Frontend und Website (erstes Feature zum Testen: Angebotserstellung)
- Logging und Monitoring ausbauen (Dashboards, Container Insights, WAF-Logs)
- Eigenes SES Configuration Set für Bounces/Beschwerden mit Alarm (ersetzt "my-first-configuration-set")
- Runbooks (Deploy, Rollback, Restore, Bastion-Zugriff), Datenschutz
- Content-Security-Policy für das CRM

### Kosten optimieren (nach ein paar Monaten stabilem Betrieb)
- Container auf ARM/Graviton umstellen (ca. -6 EUR/Monat, sobald die Pipeline baut)
- Compute Savings Plan 1 Jahr für Fargate (ca. -6 bis -9 EUR/Monat)
- RDS Reserved Instance 1 Jahr (ca. -5 EUR/Monat)

### Doku
- README mit Setup und Env-Tabelle (inkl. HELIX_TAX_NUMBER)
- JavaDoc und Confluence-Seite
- PDF mit Infra-Übersicht

### Website
- Website-Chatbot: n8n-AI durch Google-MCP ersetzen

## Future
- Mitarbeiter-Seite mit Suche und Filter
- Angebotserstellung für Mandanten (am Projekt oder direkt am Mandant) - erstes Feature, mit dem die Pipeline getestet wird
- Dokumentenspeicherung von Mandanten
- Rechnungen: Pauschal-Zahlungen monatlich (hängen an einem bestimmten Projekt)
- E-Rechnung (XML)
- Eingangsrechnungen
- Firmendaten im CRM pflegen (Einstellungen statt Konfiguration)
- Account-Details für Consulting mit Timeline
- Teams für Rollen, Projekte und Mandanten
- Team auf der Website
- VPN (Netzwerk und Firewall) + Verstecken vor der Öffentlichkeit
