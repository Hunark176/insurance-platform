# Architektur

Die Anwendung ist ein modularer Monolith. Jede Fachdomäne besitzt ein eigenes
Spring-Modul mit einer kleinen öffentlichen API. Direkte Aufrufe zwischen
Modulen laufen ausschließlich über `XxxApi`; fachliche Rückmeldungen werden als
Events veröffentlicht.

```mermaid
flowchart LR
  customer --> policy
  product --> policy
  underwriting --> policy
  policy --> claim
  claim -- ClaimApprovedEvent --> billing
```

Der Claim-zu-Payout-Ablauf ist bewusst einseitig: `claim.application.ClaimService`
publiziert `ClaimApprovedEvent`, der Billing-Listener erzeugt idempotent eine Auszahlung.
Die Architekturprüfung in `ModularityTest` verhindert neue Zyklen und Zugriffe
auf nicht veröffentlichte Modulklassen.

Die Fachmodule verwenden eine pragmatische Schichtung: `application` koordiniert
Anwendungsfälle, `domain` enthält Fachmodelle und – wo passend – Repositories,
`controller` bildet den HTTP-Einstieg. Zusätzliche Ports und Adapter werden nur
eingesetzt, wenn sie eine echte Grenze schützen; reine Weiterleitungs-Schichten
werden vermieden.

## Lokale Profile

Das Standardprofil `dev` verwendet eine eingebettete H2-Datenbank und Hibernate
zur schnellen Entwicklung. Das Profil `local` verwendet PostgreSQL und Flyway;
die Migrationen liegen unter `backend/src/main/resources/db/migration`.
