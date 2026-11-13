# Insurance Platform

## Architektur-Roadmap und technisches Umsetzungs-Backlog

**Dokumenttyp:** Lebende Architektur-Roadmap und priorisiertes Engineering-Backlog
**Zuletzt aktualisiert:** 13. November 2026 (zweite Runde)
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
| R-03 | Authentifizierung und Zugriff pro Kunde            | P0        | Erledigt (Demo); OIDC zurückgestellt |
| R-04 | Einheitlicher Fehlervertrag und API-Typen          | P2        | Erledigt |
| R-05 | Billing- und Statusabläufe testen                  | P1        | Erledigt           |
| R-06 | Event-Zuverlässigkeit (Outbox)                     | P1        | Erledigt           |
| R-07 | Frontend-Anmelde- und Fachabläufe                  | P2        | Erledigt |
| R-08 | Repository-Schichten vereinheitlichen              | P3        | Erledigt           |
| R-09 | Betriebsbereitschaft und Konfiguration             | P2        | Erledigt           |
| R-10 | Dokumentation synchron halten                      | P3        | Fortlaufend        |
| R-11 | Deckungsregeln (Storno, Jahreshöchstleistung)      | P1        | Erledigt           |
| R-12 | Missbrauchsschutz der öffentlichen Demo            | P1        | Erledigt           |
| R-13 | Anteilige Prämienerstattung bei Stornierung        | P2        | Erledigt |
| R-14 | Idempotency-Key für schreibende API-Aufrufe        | P3        | Erledigt |
| R-15 | Demodaten regelmäßig zurücksetzen                  | P3        | Erledigt |
| R-16 | Paginierung für Policen, Kunden und Auszahlungen   | P3        | Erledigt |
| R-17 | Überwachung fehlgeschlagener Event-Zustellungen    | P3        | Erledigt |
| R-18 | PostgreSQL-Upgrade der Produktion                  | P3        | Erledigt (Anleitung) |

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

**Zurückgestellt (bewusst):** produktiver Identitätsanbieter (OIDC/JWT) und
kundengenaue Einschränkung der **lesenden** Endpunkte. Die Demo soll ohne
Anmeldung lesbar sein, und ein Identitätsanbieter wäre zusätzliche
Infrastruktur ohne Nutzen für die Portfolio-Demo. `@PreAuthorize` und
`CustomerAccessPolicy` bleiben bei einem späteren Wechsel unverändert.

### R-04 – Einheitlicher Fehlervertrag und API-Typen

**Status:** Erledigt am 13.11.2026

**Erledigt:** Fachliche, Validierungs-, Konflikt- und Rate-Limit-Fehler als
`ProblemDetail`; `IllegalStateException` gilt als Programmierfehler (500);
Frontend-Typ `Claim` enthält Status und Police.

Am 13.11.2026 ergänzt: 401/403 der Security-Filterkette ebenfalls als
`ProblemDetail` (`ProblemDetailSecurityHandler`). Frontend-Typen werden aus dem
exportierten OpenAPI-Vertrag erzeugt (`npm run generate:api-types`); die CI
schlägt fehl, wenn Vertrag und Typen auseinanderlaufen.

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

**Status:** Erledigt am 13.11.2026

**Erledigt:** Login mit Demo-Zugängen, Benutzermenü, Claim-Status mit
rollenabhängigen Aktionen, Fehlermeldungen für 401/403/409/422/429, Vitest.

Am 13.11.2026 ergänzt: Schaden melden (Kunden nur für eigene Policen, mit
Idempotency-Key), Police stornieren mit Wirksamkeitsdatum, Seitennavigation.
Nicht geplant: Detail- und Billing-Ansichten, solange sie der Demo nichts
hinzufügen.

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

**Status:** Erledigt am 13.11.2026

`PolicyCancelledEvent` → Billing erstellt genau eine Gutschrift: Prämie ×
nicht mehr gedeckte Tage / Tage der Laufzeit, kaufmännisch auf Cent gerundet
(V18, `GET /api/credit-notes`). Festgelegt: Die Prämie gilt für die gesamte
Laufzeit, abgerechnet wird taggenau.

### R-14 – Idempotency-Key für schreibende API-Aufrufe

**Status:** Erledigt am 13.11.2026 – [ADR 0006](../adr/0006-idempotency-key.md)

Header `Idempotency-Key` für `POST /api/**` (V17); Wiederholungen liefern die
gespeicherte Antwort. Ein Test belegt: vier gleichzeitige Anfragen legen genau
einen Claim an.

### R-15 – Demodaten regelmäßig zurücksetzen

**Status:** Erledigt am 13.11.2026 – [Betrieb](../operations.md)

`ops/reset-demo-data.sh --yes`: zuerst Backup, dann Leeren und Neueinspielen
in **einer** Transaktion; bei einem Fehler bleiben die alten Daten. Läuft nur
manuell oder per selbst eingerichtetem Cron. Ein PostgreSQL-Test prüft Reset
und Rollback.

### R-16 – Paginierung für weitere Listen

**Status:** Erledigt am 13.11.2026

Policen, Kunden, Auszahlungen und Gutschriften sind wie Claims paginiert
(`PageResponse`, `page`/`size` bis 100). Das Frontend nutzt Seiten zu 100.

### R-17 – Überwachung fehlgeschlagener Event-Zustellungen

**Status:** Erledigt am 13.11.2026

`EventPublicationHealthIndicator`: `/actuator/health` meldet `DOWN` und loggt
einen Fehler, wenn Events länger als 30 Minuten unvollständig sind; die
Readiness bleibt unberührt.

### R-18 – PostgreSQL-Upgrade der Produktion

**Status:** Erledigt (Anleitung) am 13.11.2026 – [Betrieb](../operations.md)

Schrittweise Anleitung für Dump und Restore in ein neues Volume mit Rückweg.
Die Durchführung selbst braucht ein Wartungsfenster und erfolgt bei Bedarf.

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
| 2026-11-13 | R-04, R-07, R-13 bis R-18 erledigt: ProblemDetail für 401/403, erzeugte API-Typen, Schaden melden und Stornieren im Frontend, Prämiengutschrift, Idempotency-Key, Demodaten-Reset, Paginierung, Event-Überwachung, Upgrade-Anleitung; R-03 (OIDC) bewusst zurückgestellt |

## 7. Verwandte Dokumente

- [Technische Architektur- und Schnittstellendokumentation](technical-documentation.md)
- [Architekturprinzipien und Klassenplatzierung](architecture-principles.md)
- [Architektur-Kurzüberblick](../architecture.md)
- [ADRs](../adr)
