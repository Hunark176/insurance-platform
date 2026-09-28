# ADR 0004: Bestehende Claims sicher an Policen anbinden

**Status:** Angenommen
**Datum:** 2026-09-28

## Kontext

Die ursprüngliche Claims-Tabelle enthält eine Kundennummer und eine optionale
`policy_id`. Die neue Claim-Fachlogik benötigt eine verbindliche
Policenreferenz. Da bestehende Schadenfälle erhalten bleiben müssen, dürfen
Migrationen weder eine Police raten noch nicht zuordenbare Datensätze löschen.
Die Flyway-Versionen `V1` bis `V7` sind bereits vorhanden und werden nicht
rückwirkend geändert.

## Entscheidung

- Die Anbindung erfolgt mit einer neuen, additiven Flyway-Migration `V8`.
- Ein bestehender Claim wird automatisch nur dann über seine Kundennummer
  einer Police zugeordnet, wenn der Kunde genau eine Police besitzt.
- Fehlender Kunde, keine bzw. mehrere Policen oder eine widersprüchliche
  vorhandene `policy_id` lassen die Migration mit einer Fehlermeldung
  fehlschlagen. Die Daten müssen vor einem erneuten Versuch fachlich geklärt
  werden.
- Bekannte historische Claim-Typen werden explizit auf die Enum-Werte
  normalisiert. Unbekannte Werte stoppen die Migration.
- Ein fehlendes Schadendatum wird aus `created_at` abgeleitet. Dieser Wert ist
  ausdrücklich nur ein historischer Fallback und nicht zwingend das tatsächliche
  Schadendatum.
- `customer_number` bleibt vorerst in der Datenbank, damit die bisherige
  Information verfügbar bleibt. Sie wird nicht mehr als aktuelles Java-
  Entity-Feld verwendet.

## Konsequenzen

- Eindeutig zuordenbare Datensätze können ohne manuelle Einzelzuordnung migriert
  werden.
- Mehrdeutige oder inkonsistente Daten werden nicht stillschweigend verändert;
  ein Betreiber muss sie vor der Migration bereinigen oder explizit zuordnen.
- Die `customer_number`-Spalte ist zunächst redundant, vermeidet aber einen
  irreversiblen Informationsverlust.
- `occurred_on` kann für Altdaten nur angenähert sein. Fachliche Verwendung
  dieses Datums muss diese Einschränkung berücksichtigen.
- Die Migration muss gegen eine frische und repräsentativ befüllte PostgreSQL-
  Datenbank getestet werden, bevor sie als produktionsbereit gilt.

## Alternativen

- Eine vorhandene Flyway-Migration rückwirkend ändern oder umbenennen: verworfen,
  weil bereits initialisierte Datenbanken dadurch inkonsistent werden können.
- Bei mehreren Policen automatisch die erste, älteste oder aktive Police wählen:
  verworfen, weil das eine fachliche Zuordnung erfinden würde.
- `customer_number` beim Upgrade sofort löschen: verworfen, weil damit
  historische Information unnötig früh entfernt würde.
- Claims ohne bekannte Police löschen oder mit einer Platzhalter-Police
  verknüpfen: verworfen, weil beides Geschäftsdaten verfälschen würde.
