# ADR 0005: Deckungsregeln – Schadenereignisprinzip und Jahreshöchstleistung

- **Status:** Angenommen
- **Entschieden am:** 12. November 2026
- **Geltungsbereich:** Module `policy` und `claim`

## Kontext

Die Deckungsprüfung hatte zwei fachliche Lücken:

1. **Stornierung wirkte rückwirkend.** Gedeckt war ein Schaden nur, wenn die Police
   zum Prüfzeitpunkt den Status `ACTIVE` hatte. Nach einer Stornierung ließ sich ein
   offener Claim für einen Schaden aus der Vertragslaufzeit nicht mehr genehmigen,
   und ein solcher Schaden konnte nicht mehr gemeldet werden.
2. **Keine Jahreshöchstleistung.** Geprüft wurde nur die Deckungssumme je
   Schadenfall. Beliebig viele Schäden einer Police konnten zusammen ein Vielfaches
   der Deckungssumme auslösen.

## Entscheidung

### Schadenereignisprinzip

Für alle Produkte der Plattform (Kfz, Hausrat, Haftpflicht, Leben) entscheidet das
**Schadendatum**, nicht der Bearbeitungszeitpunkt:

- Gedeckt ist ein Schaden mit Datum zwischen `valid_from` und dem früheren Datum aus
  `valid_to` und `cancelled_on`, jeweils einschließlich (`Policy.covers`).
- Eine Stornierung erhält ein Wirksamkeitsdatum (`cancelled_on`, letzter gedeckter
  Tag). Rückwirkende Stornierungen werden abgelehnt.
- Offene Claims bleiben von einer Stornierung unberührt; Schäden aus der Laufzeit
  können nachgemeldet werden.

### Jahreshöchstleistung

- Ein Produkt kann einen Faktor `annual_aggregate_factor` haben (z. B. 2 =
  „2-fache Maximierung“). Bei Ausstellung übernimmt die Police den Betrag
  `Deckungssumme × Faktor` als `annual_aggregate_limit`, so wie die Deckungssumme
  selbst. Spätere Produktänderungen ändern bestehende Verträge nicht.
- Versicherungsjahre beginnen am Jahrestag von `valid_from` (`PolicyYear`). Ein
  Claim zählt zum Jahr seines **Schadendatums**.
- Bei der Genehmigung gilt: Betrag ≤ Deckungssumme **und** Betrag ≤ Jahreshöchstleistung
  minus bereits genehmigte Beträge desselben Versicherungsjahres (`Claim.approve`).
- In den Demodaten hat die private Haftpflicht den Faktor 2; die übrigen Produkte
  haben nur die Deckungssumme je Schadenfall.

### Nebenläufigkeit

Vor der Summenbildung sperrt die Genehmigung die Police
(`PolicyApi.lockForClaimApproval`, `SELECT … FOR UPDATE`). Gleichzeitige
Genehmigungen für dieselbe Police laufen dadurch nacheinander, und jede sieht die
Beträge der anderen. `AnnualAggregateIntegrationTest` belegt das; ohne Sperre werden
dort beide Genehmigungen angenommen.

## Konsequenzen

- Die Deckungsfrage hängt nicht mehr vom aktuellen Status ab; `PolicyApi.isActiveAt`
  heißt jetzt `isCoveredOn`.
- Genehmigungen derselben Police werden serialisiert. Bei der erwarteten Last ist das
  unkritisch; die Sperre gilt nur für die Dauer der Genehmigungstransaktion.
- Nicht umgesetzt: anteilige Prämienerstattung bei Stornierung (§ 39 VVG), eine
  Verbrauchsregel, bei der sich die Deckungssumme nach einem Schaden dauerhaft
  verringert, und das Claims-made-Prinzip (z. B. D&O), bei dem das Meldedatum zählt.
  Die Plattform bietet keine Produkte an, die diese Regeln benötigen.

Diese Regeln bilden übliche Praxis ab. Für echte Produkte sind die jeweiligen
Versicherungsbedingungen maßgeblich.
