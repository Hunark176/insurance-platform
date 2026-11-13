# Insurance Platform

## Architektur-Roadmap und technisches Umsetzungs-Backlog

**Dokumenttyp:** Lebende Architektur-Roadmap und priorisiertes Engineering-Backlog
**Zuletzt aktualisiert:** 13. November 2026
**Geltungsbereich:** Insurance-Platform-Repository
**Status:** Planungsgrundlage – offene Punkte sind keine Aufforderung, sie sofort umzusetzen

## 1. Zweck und Pflege

Dieses Dokument verbindet eine **Architektur-Roadmap**, ein
**Technical-Debt-/Risiko-Register** und Verweise auf **ADRs**. Es ist kein
Sprintplan und enthält keine erfundenen Termine. Diese Markdown-Datei ist die
maßgebliche Fassung.

### 1.1 Statuswerte

| Status                 | Bedeutung                                                           |
| ---------------------- | ------------------------------------------------------------------- |
| **Offen**              | Problem bekannt; Umfang und Abnahmekriterien noch zu klären         |
| **Bereit**             | Problem verstanden, Umfang und Abnahmekriterien geklärt             |
| **In Arbeit**          | Änderung wird umgesetzt                                             |
| **Teilweise erledigt** | Ein abgegrenzter Teil ist umgesetzt; der Rest ist beschrieben       |
| **Erledigt**           | Abnahmekriterien erfüllt, Tests und Dokumentation aktualisiert      |
| **Zurückgestellt**     | Bewusst nicht umgesetzt; Grund dokumentiert                         |

Ein Punkt wird erst **Erledigt**, wenn Abnahmekriterien und Tests erfüllt sind.
Erledigte Punkte werden nicht gelöscht, sondern mit Datum und Ergebnis
festgehalten.

### 1.2 Umgang mit dem Backlog

1. Vor Beginn die angegebenen Codepfade und Tests lesen und prüfen, ob der Punkt
   inzwischen überholt ist.
2. Sicherheits-, Datenschutz- und Fachentscheidungen nicht erraten.
3. Bei mehreren ernsthaften Lösungswegen vorher ein ADR schreiben.
4. Klein und überprüfbar ändern; Test ergänzen; Dokumentation mit dem Code
   aktualisieren.
5. Status, Ergebnis und Datum hier pflegen.

## 2. Architekturleitplanken

- **Modularer Monolith beibehalten.** Ein deploybares System ist einfacher zu
  entwickeln und zu betreiben als vorzeitig getrennte Microservices.
- **Zwei Kommunikationsarten bewusst verwenden:**

  ```text
  Sofort benötigte Antwort: synchroner Modulvertrag (XxxApi)
  Bereits eingetretenes Ereignis: Domain Event über die Outbox --> reagierende Module
  ```

- **Modulverträge klein und explizit halten;** Listen per Batch laden.
- **Fachregeln im Domänenmodell,** Koordination im Application-Service.
- **Verhalten durch Tests absichern,** inklusive PostgreSQL und Nebenläufigkeit,
  wo Regeln über mehrere Datensätze gelten.
- **Produktionstauglichkeit durch Anforderungen definieren.** Zusätzliche
  Infrastruktur erst bei konkretem Bedarf.

```text
REST /api (Login, Rollen, Rate-Limit)
   |
   v
Modularer Monolith
   |-- synchron: Policy --> Customer, Product, Underwriting
   |-- synchron: Claim  --> Policy (Snapshot, Deckung, Sperre bei Genehmigung)
   |
   |-- PolicyIssuedEvent ---[Outbox]--> Billing: genau eine Rechnung
   `-- ClaimApprovedEvent --[Outbox]--> Billing: genau eine Auszahlung
```

## 3. Übersicht

| ID   | Thema                                              | Priorität | Status             |
| ---- | -------------------------------------------------- | --------- | ------------------ |
| R-01 | Flyway-Versionen und PostgreSQL-Migrationen        | P1        | Erledigt           |
| R-02 | Entity-, Seed- und Schemaabgleich                  | P1        | Erledigt           |
| R-03 | Authentifizierung und Zugriff pro Kunde            | P0        | Teilweise erledigt |
| R-04 | Einheitlicher Fehlervertrag und API-Typen          | P2        | Teilweise erledigt |
| R-05 | Billing- und Statusabläufe testen                  | P1        | Erledigt           |
| R-06 | Event-Zuverlässigkeit (Outbox)                     | P1        | Erledigt           |
| R-07 | Frontend-Anmelde- und Fachabläufe                  | P2        | Teilweise erledigt |
| R-08 | Repository-Schichten vereinheitlichen              | P3        | Erledigt           |
| R-09 | Betriebsbereitschaft und Konfiguration             | P2        | Erledigt           |
| R-10 | Dokumentation synchron halten                      | P3        | Fortlaufend        |
| R-11 | Deckungsregeln (Storno, Jahreshöchstleistung)      | P1        | Erledigt           |
| R-12 | Missbrauchsschutz der öffentlichen Demo            | P1        | Erledigt           |
| R-13 | Anteilige Prämienerstattung bei Stornierung        | P2        | Offen              |
| R-14 | Idempotency-Key für schreibende API-Aufrufe        | P3        | Offen              |
| R-15 | Demodaten regelmäßig zurücksetzen                  | P3        | Offen              |
| R-16 | Paginierung für Policen, Kunden und Auszahlungen   | P3        | Offen              |
| R-17 | Überwachung fehlgeschlagener Event-Zustellungen    | P3        | Offen              |
| R-18 | PostgreSQL-Upgrade der Produktion                  | P3        | Offen              |

## 4. Punkte im Detail

### R-01 – Flyway-Versionen und PostgreSQL-Migrationen

**Status:** Erledigt am 12.11.2026

Die Historie V1–V16 ist eindeutig. `PostgresPersistenceIntegrationTest` führt
alle Migrationen gegen PostgreSQL 15 aus und startet die Anwendung mit
`ddl-auto: validate`. Der Test wurde zuvor in der CI still übersprungen
(Testcontainers zu alt für das Docker der Runner); seit Testcontainers 1.21.4
läuft er dort und darf nicht mehr übersprungen werden. Lokal, CI und Produktion
verwenden PostgreSQL 15.

**Weiterhin sinnvoll:** Upgrade-Test mit repräsentativ befüllten Altdaten vor
größeren Datenmigrationen.

### R-02 – Entity-, Seed- und Schemaabgleich

**Status:** Erledigt am 12.11.2026

`ddl-auto: validate` gegen PostgreSQL bestätigt Entities und Schema. Das
H2-Seed (`dev-data.sql`) spiegelt fachlich relevante Migrationen wie V16.

### R-03 – Authentifizierung und Zugriff pro Kunde

**Status:** Teilweise erledigt am 12.11.2026

**Erledigt:** HTTP Basic mit Demo-Zugängen aus der Konfiguration, Rollen pro
Endpunkt (`@PreAuthorize`), Eigentumsprüfung für Kunden
(`CustomerAccessPolicy`), Tests für 401/403/200 mit echten Zugängen.

**Offen:** produktiver Identitätsanbieter (OIDC/JWT), Zuordnung echter Benutzer
zu Kunden, kundengenaue Einschränkung auch der **lesenden** Endpunkte. Diese
sind für die Demo bewusst öffentlich.

### R-04 – Einheitlicher Fehlervertrag und API-Typen

**Status:** Teilweise erledigt am 12.11.2026

**Erledigt:** Fachliche, Validierungs-, Konflikt- und Rate-Limit-Fehler als
`ProblemDetail`; `IllegalStateException` gilt als Programmierfehler (500);
Frontend-Typ `Claim` enthält Status und Police.

**Offen:**

- 401 aus der Security-Filterkette hat keinen Body, 403 durch `denyAll`
  (`DELETE`) liefert das Spring-Standardformat statt `ProblemDetail`.
  Abnahme: eigener `AuthenticationEntryPoint` und `AccessDeniedHandler`, Tests.
- Frontend-Typen werden von Hand gepflegt. Abnahme: aus `/v3/api-docs`
  generierte Typen (z. B. `openapi-typescript`) mit CI-Prüfung auf Abweichungen.

### R-05 – Billing- und Statusabläufe testen

**Status:** Erledigt am 12.11.2026

Erfolgs- und Ablehnungspfade sind durch Domain-, Service- und
Integrationstests abgedeckt: Statusmaschine, Deckung, Deckungssumme,
Jahreshöchstleistung, Stornierung, genau eine Rechnung/Auszahlung pro
Ereignis, Wiederholung nach Listener-Ausfall.

### R-06 – Event-Zuverlässigkeit

**Status:** Erledigt am 12.11.2026 – [ADR 0002](../adr/0002-spring-modulith-events-outbox.md)

Persistente Event Publication Registry (V13), Wiederholung beim Start und zur
Laufzeit, Aufräumen nach sieben Tagen. `EventOutboxIntegrationTest` belegt,
dass ein Event bei Listener-Ausfall erhalten bleibt und danach zugestellt wird.

### R-07 – Frontend-Anmelde- und Fachabläufe

**Status:** Teilweise erledigt am 12.11.2026

**Erledigt:** Login mit Demo-Zugängen, Benutzermenü, Claim-Status mit
rollenabhängigen Aktionen, Fehlermeldungen für 401/403/409/422/429, Vitest.

**Offen (nach Bedarf):** Schadenmeldung und Policenstornierung in der UI,
Detailansichten, Billing-Ansicht. Erst umsetzen, wenn der Nutzen für die Demo
feststeht.

### R-08 – Repository-Schichten vereinheitlichen

**Status:** Erledigt am 28.10.2026 – [ADR 0003](../adr/0003-persistenzzugriff-im-domain-modul.md)

### R-09 – Betriebsbereitschaft und Konfiguration

**Status:** Erledigt am 12.11.2026

Health/Readiness über Actuator (nur intern), Container-`HEALTHCHECK`, Caddy
wartet auf gesundes Backend, Nicht-Root-Container, JSON-Logs in `prod`,
Deployment nur nach grüner CI, keine parallelen Deployments, Image-Smoke-Test
in Pull Requests. Lokales Compose wartet auf eine gesunde Datenbank.

### R-10 – Dokumentation synchron halten

**Status:** Fortlaufend; vollständig aktualisiert am 13.11.2026

Veraltete PDF-Exporte und Platzhalter-Dokumente wurden entfernt. Die
Markdown-Dateien sind die einzige Fassung.

### R-11 – Deckungsregeln

**Status:** Erledigt am 12.11.2026 – [ADR 0005](../adr/0005-deckungsregeln.md)

Stornierung mit Wirksamkeitsdatum nach dem Schadenereignisprinzip (V15),
Jahreshöchstleistung pro Versicherungsjahr mit Sperre gegen parallele
Genehmigungen (V16). Ein Nebenläufigkeitstest schlägt ohne Sperre nachweislich
fehl.

### R-12 – Missbrauchsschutz der öffentlichen Demo

**Status:** Erledigt am 13.11.2026

`RateLimitFilter`: 30 schreibende und 300 lesende Requests je Client-IP und
Minute, vor Spring Security (zählt auch falsche Logins), Antwort 429 mit
`Retry-After`.

### R-13 – Anteilige Prämienerstattung bei Stornierung

**Priorität:** P2 · **Status:** Offen

Bei vorzeitiger Beendigung steht die Prämie nur bis zum Vertragsende zu
(§ 39 VVG). Heute ändert eine Stornierung die Rechnung nicht.

**Vorschlag:** `PolicyCancelledEvent` mit Wirksamkeitsdatum; Billing erstellt
eine Gutschrift für den nicht verbrauchten Zeitraum. **Vorher klären:**
Abrechnungsperiode (jährlich/monatlich), Rundung, Ausnahmen.

### R-14 – Idempotency-Key für schreibende API-Aufrufe

**Priorität:** P3 · **Status:** Offen

Wiederholt ein Client nach einem Timeout `POST /api/claims`, entsteht ein
zweiter Claim. **Abnahme:** optionaler Header `Idempotency-Key`, gespeicherte
Antwort je Schlüssel und Benutzer mit Ablaufzeit, Test für Wiederholung.

### R-15 – Demodaten regelmäßig zurücksetzen

**Priorität:** P3 · **Status:** Offen

Besucher können Demodaten verändern. Ein automatisches Zurücksetzen löscht
Daten und ist deshalb bewusst nicht im Backend eingebaut. **Vorschlag:**
dokumentiertes Skript, das per Cron auf dem Server Geschäftsdaten leert und die
Demodaten neu einspielt; vorher Backup.

### R-16 – Paginierung für weitere Listen

**Priorität:** P3 · **Status:** Offen

`GET /api/policies`, `/api/customers` und `/api/payouts` liefern alle Einträge.
Bei wachsenden Daten wie bei Claims paginieren; das ändert den API-Vertrag und
erfordert eine Anpassung des Frontends.

### R-17 – Überwachung fehlgeschlagener Event-Zustellungen

**Priorität:** P3 · **Status:** Offen

Dauerhaft fehlschlagende Events werden wiederholt und erzeugen Warnungen im
Log, aber keinen Alarm. **Abnahme:** Kennzahl für unvollständige Einträge in
`event_publication` (z. B. Actuator-Health-Indikator oder Metrik) und eine
dokumentierte Reaktion.

### R-18 – PostgreSQL-Upgrade der Produktion

**Priorität:** P3 · **Status:** Offen

Produktion läuft auf PostgreSQL 15. Ein Major-Upgrade erfordert Dump und
Restore des Volumes und gemeinsame Anpassung von Compose und Testcontainers.

## 5. Bewusst nicht auf der Liste

- die Anwendung in Microservices zerlegen,
- einen Message Broker wie Kafka einführen,
- Hibernate/JPA gegen JDBC oder jOOQ austauschen,
- alle Module gleichzeitig auf eine neue Architektur umschreiben,
- Frontend-Funktionen ohne klaren Nutzen für die Demo hinzufügen.

## 6. Änderungshistorie

| Datum      | Änderung                                                                                   |
| ---------- | ------------------------------------------------------------------------------------------ |
| 2026-09-28 | Roadmap und Kommunikationsleitplanken erfasst; V8-Backfill-Regel ergänzt                   |
| 2026-10-28 | Paketstruktur vereinheitlicht, Migrationen bis V10, lokale/produktive Bereitstellung         |
| 2026-11-12 | Security mit Rollen und Eigentumsprüfung, Outbox, Deploy nach CI, N+1 behoben, UTC-Zeitstempel, Deckungsregeln, Health-Checks, Frontend-Tests; R-01, R-02, R-05, R-06, R-09, R-11 erledigt |
| 2026-11-13 | Rate-Limit (R-12); Dokumentation vollständig aktualisiert; neue offene Punkte R-13 bis R-18 |

## 7. Verwandte Dokumente

- [Technische Architektur- und Schnittstellendokumentation](technical-documentation.md)
- [Architekturprinzipien und Klassenplatzierung](architecture-principles.md)
- [Architektur-Kurzüberblick](../architecture.md)
- [ADRs](../adr)
