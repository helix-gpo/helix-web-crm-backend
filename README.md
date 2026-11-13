# Todo

## Vor dem Go-Live

### 2. AWS
- ~~Grundlagen: SSO-Zugang, Terraform-State, Netzwerk, Security Groups, RDS, ECR~~
- ~~Secrets Manager, IAM-Rollen, S3 Prod-Bucket, SES (übernommen), Cognito Prod (+ Werte in CRM environment.prod.ts)~~
- ~~Bastion testen: Tunnel aufbauen, App-User `helix_app` per SQL anlegen~~
- ~~Zertifikate, Load Balancer (alte API bleibt bis zum Go-Live aktiv)~~
- ~~ECS Fargate (Backend + Website), erstes Image manuell nach ECR, Admin-Bootstrap (Felix + Helix GPO)~~
- ~~CRM-Frontend auf S3 + CloudFront (crm.helix-gpo.com), Cognito Managed Login mit Branding + Deutsch, WAF~~
- ~~Lokal auf MySQL 8.4 umstellen (docker-compose + Testcontainers), passend zu RDS~~

### 3. Betrieb (Minimum vor Go-Live)
- ~~Alarme: Backend/Website down (Target Group unhealthy), 5xx-Fehler, RDS CPU/Speicher, an Mail~~
- ~~Backup-Restore einmal testen (RDS Point-in-Time-Restore in eine Test-Instanz, danach löschen)~~

### 4. Go-Live (13.11.2026)
- ~~USt-IdNr. klären: beim Steuerberater beantragt, Start mit Steuernummer (USt-IdNr. im Secret leer)~~
- ~~Mail-Texte final mit dem Geschäftspartner~~
- ~~Abnahme-Durchlauf vor der Umstellung (per hosts-Datei)~~
- ~~Chatbot und Feedback-Link im Footer bis zum Relaunch ausgeblendet (Website-Image 0be47bb)~~
- ~~DNS umstellen: api, www und apex per Terraform (dns.tf) auf den neuen Load Balancer, alte AAAA-Einträge gelöscht~~
- ~~hosts-Datei-Einträge entfernen, alte Website ist damit ersetzt~~
- Abnahme nach der Umstellung: Henry als Mitarbeiter einladen, Referenz-Link (Mail -> /feedback -> Abschicken), Rechnung ausstellen + per Mail mit PDF (erste echte Nummer RE-2026-00001)

## Danach

### Aufräumen
- ~~Alte Infrastruktur abgebaut: Staging-ECS + ALB, Staging-RDS, alte CloudFront/S3-Website, alte Bild-Buckets, ECR-Repo, Log-Groups, Security Groups, Zertifikate, n8n-Eintrag~~
- ~~IAM: User AWS-CLI-ECR und AWS-DEV-BUCKETS-USER, alte Rollen und Policies gelöscht~~
- Lokal: Block [default] aus C:\Users\felix\.aws\credentials entfernen (Keys ungültig)
- Prüfen, ob Bucket helix-gpo-crm-documents von der lokalen Entwicklung genutzt wird (sonst löschen)
- DMARC von p=none auf p=quarantine verschärfen
- Cognito-Branding: 8.0/12.0 in branding-settings.json als 8/12 schreiben (Dauer-Differenz im Plan)
- Optional: VPN (WireGuard + WAF-IP-Sperre für CRM und /api/crm)
- Optional: ALB auf Dual-Stack/IPv6 (VPC + Subnets brauchen IPv6-Bereiche)

### Pipeline und Betrieb
- Pipeline für Backend, CRM-Frontend und Website (erstes Feature zum Testen: Angebotserstellung)
- Logging und Monitoring ausbauen (Dashboards, Container Insights, WAF-Logs)
- Eigenes SES Configuration Set für Bounces/Beschwerden mit Alarm (ersetzt "my-first-configuration-set")
- Runbooks (Deploy, Rollback, Restore inkl. Ergebnis Restore-Test, Bastion-Zugriff), Datenschutz
- Content-Security-Policy für das CRM
- USt-IdNr. eintragen, sobald da: Secret helix-prod/company (HELIX_VAT_ID), danach
  aws ecs update-service --cluster helix-prod --service backend --force-new-deployment

### Kosten optimieren (nach ein paar Monaten stabilem Betrieb)
- Container auf ARM/Graviton umstellen (ca. -6 EUR/Monat, sobald die Pipeline baut)
- Compute Savings Plan 1 Jahr für Fargate (ca. -6 bis -9 EUR/Monat)
- RDS Reserved Instance 1 Jahr (ca. -5 EUR/Monat)

### Doku
- README mit Setup und Env-Tabelle (inkl. HELIX_TAX_NUMBER)
- Cheatsheet: ECS-Service-Namen backend/website, Website-Deploy (Build, Push, Tag in variables.tf)
- JavaDoc und Confluence-Seite
- PDF mit Infra-Übersicht

### Website
- Website-Chatbot: n8n-AI durch Google-MCP ersetzen und wieder einbauen
- Feedback-Link wieder einbauen (sinnvoll eingebunden, Route /feedback existiert weiter)

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
