# ADR 0002: Spring Modulith Events mit persistenter Outbox

- **Status:** Angenommen, Outbox umgesetzt am 12. November 2026
- **Geltungsbereich:** Modulübergreifende Domänenereignisse

## Kontext

Domänenereignisse werden mit Spring Modulith veröffentlicht. Listener sind
idempotent und erzeugen keine Rückaufrufe in das sendende Modul. Die Listener
(`@ApplicationModuleListener`) laufen asynchron nach dem Commit der
auslösenden Transaktion.

Ohne persistente Event-Publikation ging ein Ereignis verloren, wenn die
Anwendung zwischen Commit und Listener abstürzte oder der Listener fehlschlug:
Ein genehmigter Claim blieb dann ohne Auszahlung, eine ausgestellte Police ohne
Rechnung, und der Fehler war nur im Log sichtbar.

## Entscheidung

- Die relationale Event Publication Registry von Spring Modulith
  (`spring-modulith-starter-jpa`) ist aktiv. Jedes Ereignis wird in derselben
  Transaktion wie die fachliche Änderung in `event_publication` gespeichert,
  je Listener ein Eintrag (Migration `V13`).
- Nach erfolgreicher Verarbeitung setzt Modulith `completion_date`. Einträge
  ohne `completion_date` gelten als unvollständig.
- Unvollständige Ereignisse werden erneut zugestellt:
  - beim Start (`spring.modulith.events.republish-outstanding-events-on-restart`),
  - zur Laufzeit periodisch durch `EventPublicationMaintenance`, sobald sie
    älter als `insurance.events.maintenance.retry-older-than` (Standard 5 Minuten)
    sind.
- Abgeschlossene Einträge werden nach
  `insurance.events.maintenance.completed-retention` (Standard 7 Tage) gelöscht.

## Konsequenzen

- Ein Ereignis wird mindestens einmal zugestellt (at-least-once). Listener
  müssen deshalb idempotent bleiben; die Billing-Listener prüfen auf vorhandene
  Datensätze und sind zusätzlich durch Unique-Constraints
  (`payouts.claim_id`, `invoices.policy_id`) abgesichert.
- Ereignisse werden als JSON gespeichert. Änderungen an Event-Records müssen
  abwärtskompatibel sein, solange noch unvollständige Einträge im alten Format
  existieren können.
- Ein dauerhaft fehlschlagender Listener wird regelmäßig erneut versucht und
  erzeugt Warnungen im Log, bis die Ursache behoben ist.
- `EventOutboxIntegrationTest` prüft Speicherung, Abschluss und erneute
  Zustellung nach einem simulierten Ausfall; `PostgresPersistenceIntegrationTest`
  prüft Migration und Mapping gegen PostgreSQL.
