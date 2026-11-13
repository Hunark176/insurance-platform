# ADR 0006: Idempotency-Key für schreibende API-Aufrufe

- **Status:** Angenommen
- **Entschieden am:** 13. November 2026
- **Geltungsbereich:** `POST /api/**`

## Kontext

Erhält ein Client auf `POST /api/claims` wegen eines Timeouts keine Antwort,
weiß er nicht, ob der Schaden angelegt wurde. Wiederholt er die Anfrage,
entsteht ein zweiter Schadenfall. Unique-Constraints helfen hier nicht, weil
zwei Meldungen desselben Schadens fachlich nicht unterscheidbar sind.

## Entscheidung

Clients können den Header `Idempotency-Key` mitsenden (8–100 Zeichen). Der
`IdempotencyFilter` gilt für alle `POST /api/**` nach der Authentifizierung:

| Situation | Antwort |
| --- | --- |
| erste Anfrage mit Schlüssel | wird ausgeführt; Status, Content-Type und Body werden gespeichert (außer bei 5xx) |
| gleiche Anfrage, gleicher Schlüssel | gespeicherte Antwort mit `Idempotency-Replayed: true`, keine erneute Ausführung |
| gleicher Schlüssel, anderer Inhalt | `422` |
| erste Anfrage läuft noch | `409` |
| 5xx oder Exception | Schlüssel wird freigegeben, erneuter Versuch möglich |
| ohne Header | unverändert |

- Gültigkeitsbereich: Benutzer, Pfad und Schlüssel; Aufbewahrung 24 Stunden.
- Vor der eigentlichen Anfrage wird in eigener Transaktion ein Datensatz mit
  eindeutigem `scope_key` angelegt (`idempotency_records`, V17). Gleichzeitige
  Erstanfragen scheitern am Unique-Constraint und erhalten `409`.
- Nicht abgeschlossene Reservierungen gelten nach fünf Minuten als verwaist.
- Das Frontend erzeugt pro geöffnetem Meldeformular einen Schlüssel und
  verwendet ihn bei jedem Sendeversuch.

## Alternativen

- **Fachliche Duplikaterkennung** (gleiche Police, gleiches Datum, gleicher
  Betrag): verworfen, weil zwei echte Schäden gleich aussehen können.
- **Nur clientseitig doppeltes Absenden verhindern:** hilft nicht bei
  Netzwerkfehlern nach erfolgreicher Verarbeitung.
- **Pflicht-Header:** verworfen, um bestehende Clients und Swagger-Aufrufe nicht
  zu brechen.

## Konsequenzen

- Wiederholungen sind für Clients mit Schlüssel sicher; ohne Schlüssel bleibt
  das bisherige Verhalten.
- Wiedergegeben werden nur Status, Content-Type und Body, keine weiteren Header.
- Antworten über 1 MB werden nicht gespeichert.
- `IdempotencyIntegrationTest` prüft alle Fälle, darunter vier gleichzeitige
  Anfragen mit genau einem angelegten Claim.
