> **Historische Notiz (Stand 2. November 2026).** Seitdem umgesetzt: persistente Outbox (ADR 0002, PR #4). Die Idempotenz von API-Schreibaufrufen (Idempotency-Key) ist weiterhin offen.

**Kurz gesagt: Teilweise.** Es gibt eine begrenzte Deduplizierung für Folgeaktionen, aber **keine allgemeine Idempotenz für API-Aufrufe**. Eine **persistente Outbox bzw. dauerhaft gespeicherte Event-Wiederzustellung ist nicht implementiert**. Das ist im aktuellen modularen Monolithen eine bewusste Vereinfachung – aber für zuverlässige, asynchrone oder externe Geschäftsabläufe eine wichtige spätere Ausbaustufe.

### 1. Was „idempotent“ bedeutet

Eine Aktion ist idempotent, wenn ihre mehrfache Ausführung dasselbe Ergebnis hat wie eine einmalige Ausführung. Das ist wichtig, wenn ein Client nach einem Timeout nicht weiß, ob der Server den Vorgang bereits verarbeitet hat, und ihn wiederholt.

Bei euch gibt es derzeit **keinen allgemeinen `Idempotency-Key`** für Schreibaufrufe. Wiederholt ein Client beispielsweise `POST /api/claims` oder die Policenerstellung, kann dadurch ein zweiter Datensatz entstehen. Die vorhandenen Rollen und Datenbank-Constraints ersetzen keine Idempotenz über wiederholte Requests hinweg.

**Begrenzt abgesichert sind die Event-Listener:**

- Bei einer Claim-Genehmigung prüft der Listener, ob es bereits eine Auszahlung für diesen Claim gibt. Zusätzlich ist `payouts.claim_id` in der Datenbank eindeutig. Eine doppelte Auszahlung wird daher stark verhindert.
- Bei einer Policenausstellung prüft der Listener, ob es bereits eine Rechnung für diese Police gibt. In der Rechnungstabelle fehlt aber derzeit ein eindeutiger Constraint auf `policy_id`. Bei gleichzeitig eintreffenden doppelten Events könnten deshalb theoretisch doppelte Rechnungen entstehen.

Die Prüfungen „gibt es schon?“ sind für den normalen Ablauf sinnvoll, aber allein keine vollständige Garantie gegen konkurrierende Wiederholungen. Die robuste Absicherung besteht üblicherweise aus einer Kombination aus Datenbank-Constraint, sicherem Duplikatverhalten und – für wiederholbare API-Schreibaufrufe – einem Idempotency-Key mit gespeichertem Ergebnis.

### 2. Was Outbox bedeutet und was bei euch fehlt

Ein Event wie `ClaimApprovedEvent` wird im Prozess veröffentlicht; ein Listener erzeugt daraus die Auszahlung. Entsprechendes passiert bei `PolicyIssuedEvent` und der Rechnung.

Eine persistente Outbox bzw. gespeicherte Event-Publikation schreibt die auszuliefernden Events dauerhaft in dieselbe Datenbanktransaktion wie die fachliche Änderung. Ein Worker kann sie anschließend ausliefern und bei einem Fehler erneut versuchen. So sinkt das Risiko, dass eine Änderung gespeichert ist, aber die Folgeaktion wegen eines Prozessabsturzes verloren geht.

Im Projekt sind Spring-Modulith-Events und `@ApplicationModuleListener` vorhanden. In der Maven-Konfiguration ist jedoch nur `spring-modulith-events-core` zu sehen; eine relationale persistente Event-Publikation und eine dazugehörige Konfiguration sind nicht nachgewiesen. Die fachlichen Events sind somit **nicht als zuverlässig wiederanlaufbare Outbox abgesichert**. Die passende ADR beschreibt die Outbox ebenfalls als mögliche spätere Ergänzung, nicht als bereits aktive Funktion.

### 3. Was das für euren jetzigen Stand heißt

Für einen **Lern- und Entwicklungs-Modular-Monolithen** ist es vertretbar, zunächst ohne Outbox zu starten, solange die Grenzen klar sind: keine verteilten Services, keine externe Zahlungsplattform und keine Garantie, dass Folgeaktionen nach einem Prozessausfall automatisch wiederholt werden.

Für **echte Auszahlungen oder Rechnungsstellung in einem produktiven Umfeld** sollte die Zuverlässigkeit höher sein. Dann würde ich zuerst Duplikate über eindeutige Datenbankregeln und Tests absichern und anschließend persistente Event-Publikation mit Wiederholung und Überwachung einführen. Ein Broker wie Kafka ist dafür nicht automatisch nötig; eine relationale, persistente Veröffentlichung passt besser zum aktuellen Monolithen.

### 4. Weitere wichtige Architekturthemen – nach Priorität

1. **Datenbank-Konsistenz und Wiederholbarkeit:** eindeutige Geschäftsregeln dauerhaft in der Datenbank absichern, besonders Rechnung pro Police; Idempotenz für kritische Schreib-APIs festlegen. Dazu gehören Tests für doppelte und gleichzeitige Aufrufe.
2. **Event-Zuverlässigkeit:** persistente Veröffentlichung, Wiederholungsstrategie und Fehlerüberwachung einführen, sobald verlorene Rechnungs-/Auszahlungsaktionen nicht akzeptabel sind.
3. **Produktionsidentität und Berechtigungen:** das Projekt nutzt laut aktueller Security-Konfiguration In-Memory-Benutzer. Für produktionsähnlichen Betrieb braucht es externe Identitäten, sichere Geheimnisverwaltung und eine klare Datenzugriffsregelung. Das ist besonders wichtig, bevor echte Kundendaten verwendet werden.
4. **PostgreSQL-Verifikation:** die gesamte Migration und die wichtigsten Abläufe gegen PostgreSQL automatisiert testen, nicht nur gegen H2. Das ist relevant, weil sich Verhalten und Constraints zwischen Datenbanken unterscheiden können.
5. **Betriebsfähigkeit:** Backups und Wiederherstellung, Healthchecks, Metriken und Alarmierung festlegen. Logs und Request-IDs helfen bei der Fehlersuche, garantieren allein aber weder Wiederherstellung noch Zustellung.
6. **Audit-Umfang fachlich klären:** Die bestehenden Audit-Einträge dokumentieren bestimmte fachliche Änderungen. Eine Änderung der Schadenssumme ist aktuell nicht implementiert; deshalb gibt es dafür weder eine Änderungsoperation noch einen dazugehörigen Audit-Eintrag. „Claim genehmigt“ und „Claim abgelehnt“ werden derzeit als Statusänderungen protokolliert, nicht als eigene Audit-Aktionen.

**Meine Empfehlung:** Microservices oder Kafka jetzt nicht vorschnell hinzufügen. Für den nächsten sinnvollen Schritt würde ich erst die Lücken bei Idempotenz und Datenbank-Constraints präzisieren und testen. Danach kann die persistente Outbox gezielt ergänzt werden, wenn ihr für Rechnungen und Auszahlungen eine Wiederzustellgarantie braucht.