# Architekturprinzipien und Klassenplatzierung

**Status:** Arbeitsgrundlage für die aktuelle Implementierung  
**Zuletzt geprüft:** 13. November 2026  
**Geltungsbereich:** Backend-Module, Frontend-Funktionen und künftige Architekturänderungen  
**Beziehung zu ADRs:** Dieses Dokument erklärt, wie mit den angenommenen Entscheidungen gearbeitet wird. Es ersetzt oder ändert kein ADR stillschweigend.

## 1. Architekturrichtung

Die Anwendung ist ein **modularer Monolith**: eine Spring-Boot-Anwendung mit
fachlichen Modulen in einem Prozess und einer relationalen Datenbank.
Spring Modulith beschreibt und prüft Modulgrenzen. Das ist die aktuelle
Architekturentscheidung; eine Aufteilung in getrennt deploybare Services ist
kein Standardziel.

**DDD wird pragmatisch dort eingesetzt**, wo es Fachbegriffe,
Geschäftsregeln und Zuständigkeiten klarer macht. Die vorhandenen Module sind
Geschäftsfähigkeitsgrenzen und mögliche Bounded Contexts. Sie belegen jedoch
nicht, dass bereits ein vollständiges Domänenmodell, eine gemeinsame
Fachsprache (Ubiquitous Language), eine Aggregate-Karte oder eine Context Map
entworfen wurden. Fachverhalten soll explizit modelliert werden, wenn echte
Invarianten bestehen; DDD-Muster sind kein Selbstzweck.

**Clean Architecture ist hier eine Leitlinie für Zuständigkeiten und
Abhängigkeiten**, keine Pflicht zu einer bestimmten Ordnerstruktur.
HTTP-spezifische Belange gehören nicht in fachliche Anwendungsfälle.
`application` koordiniert Anwendungsfälle; fachliche Invarianten liegen nah am
Domänenmodell. Modulübergreifend werden veröffentlichte Verträge statt
Persistenzdetails oder interner Klassen anderer Module verwendet.

Die Implementierung ist **keine strikt hexagonale Architektur**: JPA-Entities
und Spring-Data-Repository-Interfaces liegen gemeinsam in den
`domain`-Paketen der Module. Das ist ein bewusster Kompromiss aus
[ADR 0003](../adr/0003-persistenzzugriff-im-domain-modul.md). Die aktuelle
Domäne ist daher nicht vollständig persistenzunabhängig; die Abhängigkeiten
zeigen nicht auf jeder Klassenebene strikt nach innen.

Ziel ist eine stabile Architektur, die sich mit kleinen, begründeten
Entscheidungen weiterentwickeln lässt. Keine Architektur kann garantieren,
dass spätere Anforderungen nie eine Migration erfordern. Spekulative Schichten
sollen vermieden und notwendige Änderungen nachvollziehbar und testbar gehalten
werden.

## 2. Aktuelle Modulübersicht

Die Tabelle beschreibt den vorhandenen Code und kein idealisiertes zukünftiges
Design:

| Modul | Aktuelle Verantwortung | Veröffentlichte Verträge / beobachtete Zusammenarbeit |
|---|---|---|
| `customer` | Kundendaten und Kundenzusammenfassungen | `CustomerApi`, `CustomerSummary` |
| `product` | Produktkatalog und Produktzusammenfassungen | `ProductApi`, `ProductSummary` |
| `underwriting` | Aktuelle Eignungsprüfung für Kunde und Produkt | `UnderwritingApi`; ruft Customer- und Product-APIs auf |
| `policy` | Policenausgabe, Stornierung mit Wirksamkeitsdatum, Deckung nach Schadendatum, Versicherungsjahre | `PolicyApi`, `PolicySnapshot`, `PolicyYear`; ruft Customer-, Product-, Underwriting- und Audit-APIs auf; veröffentlicht `PolicyIssuedEvent` |
| `claim` | Schadenmeldung, Eigentumsprüfung, Claim-Statuslebenszyklus und Genehmigungsregeln | Ruft Policy- und Audit-API sowie `shared::security` auf; veröffentlicht `ClaimApprovedEvent` |
| `billing` | Rechnungen und Auszahlungen | Reagiert auf Policy-Issued- und Claim-Approved-Events; nutzt Audit-API |
| `audit` | Unveränderliche Audit-Einträge und Abfragen der Änderungshistorie | `AuditTrailApi`, `AuditContext` |
| `shared` | Querschnittsthemen für Web, Persistenz, Security und generische Domain-Hilfen | Named Interfaces `shared::domain` (`Money`, `DomainException`), `shared::web` (Fehlerbehandlung, Request-Logging) und `shared::security` (Rollen, `CustomerAccessPolicy`, Rate-Limit) |

`Underwriting` prüft derzeit eine einfache Eignungsregel. `Billing` reagiert
bei Rechnungs- und Auszahlungsanlage auf Events. Änderungen an diesen Grenzen
erfordern eine geprüfte Implementierung und gegebenenfalls ein aktualisiertes
ADR.

## 3. Modulabhängigkeiten

### Erlaubt

- Ein Modul darf die dokumentierte öffentliche API und öffentliche Werttypen
  eines anderen Moduls verwenden, zum Beispiel ein `*Api`-Interface und eine
  unveränderliche Summary oder einen Snapshot.
- Ein Modul darf ein Ereignis in seiner eigenen `events`-Named-Interface
  veröffentlichen.
- Ein konsumierendes Modul darf auf ein veröffentlichtes Ereignis in seinem
  eigenen Application-Paket mit einem Spring-Modulith-Listener reagieren.
- Module dürfen bewusst gemeinsame, eng gefasste Verträge wie die Audit-API
  und freigegebene `shared`-Pakete verwenden.
- HTTP-Controller dürfen Application-Services des eigenen Moduls aufrufen.

### Nicht erlaubt

- Kein Modul injiziert oder verwendet Repository, JPA-Entity, Mapper,
  Controller oder internen Application-Service eines anderen Moduls.
- Keine zyklischen Modulabhängigkeiten für synchrone Aufrufe in beide
  Richtungen. Zuständigkeit neu prüfen, öffentlichen Vertrag verwenden oder
  ein einseitiges Event erwägen.
- Kein Persistenzmodell eines Moduls wird als modulübergreifende API
  veröffentlicht. Benötigte Daten werden in einen kleinen, unveränderlichen
  Vertrag abgebildet.
- Geschäftsregeln werden nicht nur deshalb nach `shared` verschoben, damit
  mehrere Module darauf zugreifen können. Gemeinsamer Code muss tatsächlich
  generisch und stabil sein.
- Paket-Sichtbarkeit oder erfolgreiche Kompilierung allein beweisen nicht,
  dass eine Abhängigkeit architektonisch erlaubt ist.
  `ModularityTest.verifiesModuleBoundaries` muss bestehen. Bei unklaren Grenzen
  sind `allowedDependencies` und Named Interfaces explizit zu prüfen.

Im Code erkennbare synchrone Beziehungen sind
`underwriting -> customer/product`,
`policy -> customer/product/underwriting/audit` und
`claim -> policy/audit/shared::security`. `billing` konsumiert die Policy- und
Claim-Events und verwendet Audit. Listen über Modulgrenzen werden mit
Batch-Methoden geladen (`findByIds`, `getSnapshots`), nicht mit Einzelaufrufen in
einer Schleife. Vor einer neuen oder geänderten Kante sind Code und
Modulith-Test erneut zu prüfen.

## 4. Backend-Zuständigkeiten und Klassenplatzierung

Die vorhandene Paketstruktur bleibt maßgeblich, solange kein konkreter Bedarf
eine andere Grenze rechtfertigt:

| Verantwortung / Klasse | Aktueller Ort | Aufgabe |
|---|---|---|
| Öffentlicher Modulvertrag und unveränderliche modulübergreifende Zusammenfassung | Modulwurzel, z. B. `policy/PolicyApi.java`, `policy/PolicySnapshot.java` | Kleiner, bewusst veröffentlichter Vertrag |
| HTTP-Adapter | `<modul>/controller` | HTTP-Mapping, Request-Bindung, Validierung anstoßen, HTTP-Status; delegiert Anwendungsfälle |
| Request- und Response-Typen | `<modul>/dto` | Transportform; Entities nicht als API-Antworten wiederverwenden |
| Application-Service | `<modul>/application` | Anwendungsfall-Orchestrierung, Transaktionsgrenze, Modul-APIs, Persistenz- und Event-Koordination |
| Domain-Entity, Werttyp, Status, Repository | `<modul>/domain` | Fachzustand und Invarianten; Repository bleibt nach ADR 0003 im eigenen Modul |
| Ereignisdatentyp | `<modul>/events` | Unveränderliche Tatsache des veröffentlichenden Moduls; Event-Paket nur gezielt freigeben |
| Mapper | `<modul>/mapper` | Explizite Abbildung zwischen Transport-, Domain- und Modulvertragsmodellen |
| Querschnittliche technische Belange | `shared/<belang>` | Wiederverwendbare Infrastruktur oder generische Domain-Hilfe; keine Abhängigkeit auf Fachmodule |

Kein `usecase`-, `port`-, `adapter`-, `infrastructure`- oder
`aggregate`-Paket wird allein deshalb angelegt, weil es in einem Diagramm
vorkommt. Zuerst muss feststehen, welche Zuständigkeit vermischt ist oder
welche austauschbare Grenze tatsächlich gebraucht wird.

### Domänenmodellierung (DDD)

- Fachbegriffe aus Domäne und DTOs verwenden. Wenn die Fachsprache mehrdeutig
  ist, nachfragen, statt eine Geschäftsregel zu erfinden.
- Zustandswechsel und Invarianten nahe an den Zustand legen, den sie schützen.
  Beispiel: `Claim.approve(...)` prüft Statusübergang, Deckung, Deckungssumme und
  Jahreshöchstleistung; `Claim.transitionTo(APPROVED)` ist deshalb gesperrt.
  `Policy.cancel(...)` und `Policy.covers(...)` setzen das
  Schadenereignisprinzip um. Der Application-Service beschafft die Fakten aus
  anderen Modulen, koordiniert Persistenz, Sperren, Audit und Events und
  überlässt die Entscheidung dem Domänenobjekt.
- Verletzte Fachregeln werfen `DomainException` (HTTP 422). Eine
  `IllegalStateException` gilt als Programmierfehler (HTTP 500).
- Wertobjekte dort einsetzen, wo sie eine relevante Invariante schützen oder
  wiederkehrende fehleranfällige Verarbeitung vermeiden, wie `Money`.
  Primitive nicht ohne fachlichen Grund verpacken.
- Aggregate-Grenzen und Transaktionskonsistenz bewusst bestimmen, wenn ein
  Ablauf wächst. Nicht jede JPA-Entity ist automatisch ein gut geschnittener
  Aggregate Root.

### Clean-/Hexagonal-Abwägungen

- Controller und Persistenzmechanik dürfen nicht der Ort sein, an dem
  Geschäftsentscheidungen getroffen werden.
- Die Repository-Anordnung im `domain`-Paket bleibt bestehen. Ein
  Repository-Port mit Persistenzadapter darf nur bei einem konkreten Bedarf
  vorgeschlagen werden, zum Beispiel mehreren tatsächlich benötigten
  Persistenzimplementierungen, einer externen technischen Grenze, einem
  unabhängig zu verwaltenden Domänenmodell oder einer mit dem jetzigen Entwurf
  nicht sinnvoll testbaren Austauschgrenze.
- Ein solcher Vorschlag muss Problem, Alternativen, Betriebs- und Testfolgen
  sowie Migrationsweg beschreiben und klären, ob ADR 0003 geändert werden
  müsste. Vor der Prüfung wird er nicht implementiert.

## 5. Events und Transaktionen

- Eine synchrone öffentliche Modul-API verwenden, wenn der Aufrufer für den
  Abschluss seines Anwendungsfalls unmittelbar ein Ergebnis braucht.
- Ein einseitiges Event für eine bereits eingetretene Tatsache verwenden, auf
  die ein anderes Modul reagieren kann. Events ersetzen keine Abfrage, die zur
  Entscheidung über die Gültigkeit des auslösenden Vorgangs nötig ist.
- Event-Payloads klein und explizit halten: IDs und nur die vom Konsumenten
  benötigten Daten, keine Entities.
- Zustandsänderung und Audit-Eintrag gehören, soweit der Audit-Vertrag es
  verlangt, in dieselbe Transaktion. Application-Services legen
  Transaktionsgrenzen fest; lesende Anwendungsfälle folgen dem vorhandenen
  `readOnly`-Muster.
- Events werden über die persistente Event Publication Registry von Spring
  Modulith gespeichert (Outbox, [ADR 0002](../adr/0002-spring-modulith-events-outbox.md))
  und *at-least-once* zugestellt. Listener müssen deshalb idempotent sein und
  sollten durch einen Unique-Constraint abgesichert werden, wie
  `payouts.claim_id` und `invoices.policy_id`.
- Event-Records werden als JSON gespeichert. Änderungen an ihnen müssen
  abwärtskompatibel bleiben, solange noch unvollständige Einträge im alten
  Format existieren können.
- Wenn eine Regel über mehrere Datensätze hinweg gilt (Beispiel:
  Jahreshöchstleistung), muss der Anwendungsfall gleichzeitige Änderungen
  ausschließen, etwa mit `PolicyApi.lockForClaimApproval`, und dafür einen
  Nebenläufigkeitstest haben.
- Einen externen Message Broker nicht ohne separate Entscheidung ergänzen.
- Doppelte Anfragen nach Timeouts fängt der `Idempotency-Key` ab
  ([ADR 0006](../adr/0006-idempotency-key.md)). Neue anlegende Endpunkte sind
  damit automatisch abgedeckt; das Frontend sendet pro Vorgang einen Schlüssel.

## 6. Frontend-Zuständigkeiten

Das aktuelle Frontend ist nach Funktionen organisiert:

- `src/features/<funktion>/components` enthält die UI einer Funktion.
- `src/features/<funktion>/hooks` enthält Funktionszustand und Datenabläufe.
- `src/api` enthält Axios-API-Services und den gemeinsamen Request-Client.
- `src/types` enthält Transporttypen; `src/components/ui` und `src/shared`
  enthalten wiederverwendbare UI-Belange.
- Requests verwenden relative `/api/...`-Pfade über `apiClient`. Vite leitet
  sie in der Entwicklung weiter, Caddy `/api/*` im Deployment.
- Die App wechselt mit lokalem React-State zwischen Claim- und Policy-Ansicht;
  ein Router ist nicht konfiguriert. Funktions-Hooks verwenden React-State und
  Effects. Die installierte React-Query-Abhängigkeit wird in diesen Abläufen
  derzeit nicht genutzt.
- `src/features/auth` enthält Login-Dialog, Benutzermenü und den
  Sitzungsspeicher. Die Demo-Anmeldung liegt bewusst nur im `sessionStorage`
  des Tabs; `apiClient` hängt den Basic-Header an und meldet bei 401 ab.
- Tests liegen neben dem getesteten Code (`*.test.ts(x)`) und laufen mit
  Vitest, jsdom und Testing Library.
- API-Typen werden nicht von Hand gepflegt: `src/types` leitet sie aus
  `src/api/generated/schema.ts` ab. Nach einer Änderung an Backend-DTOs den
  Vertrag exportieren (`-Dopenapi.export=true` beim `OpenApiContractExportTest`)
  und `npm run generate:api-types` ausführen; die CI prüft das.

Neue UI folgt der vorhandenen Aufteilung in Funktion, API und Typen.
Backend-Regeln bleiben maßgeblich; Frontend-Validierung verbessert die
Bedienung, ist aber keine Sicherheits- oder Datenintegritätsgrenze. Router,
State-Bibliothek oder funktionsübergreifende Abstraktion erst ergänzen, wenn
der konkrete Navigations- oder Zustandsbedarf sie rechtfertigt.

## 7. Validierung, Fehler, Tests und Qualität

- Externe Request-Form an der HTTP-Grenze validieren; wesentliche
  Geschäftsregeln zusätzlich dort absichern, wo Domain oder Anwendungsfall es
  erfordert.
- Bestehendes `ProblemDetail`-Fehlerverhalten und Exception-Semantik erhalten.
  Fehler nicht verschlucken und nicht durch leere oder scheinbar erfolgreiche
  Antworten verdecken.
- Für zeitabhängige Geschäftsregeln die injizierte `Clock` verwenden, wo das
  bestehende Muster gilt.
- Schemaänderungen als neue, vorwärtsgerichtete Flyway-Migration umsetzen;
  bereits ausgeführte Migrationen nicht umschreiben. Bei
  datenbankspezifischen Änderungen ist das tatsächliche PostgreSQL-Verhalten
  maßgeblich.
- Gezielte Unit-Tests für Domainregeln und Anwendungsfall-Orchestrierung,
  Webtests für Request-/Autorisierungs-/Antwortverhalten,
  Persistenztests für Datenbank-Constraints und Mappings und den
  Modulith-Architekturtest bei Abhängigkeitsänderungen ergänzen. Das kleinste
  passende Testniveau wählen; erforderliche CI-Prüfungen beibehalten.
- Backend-CI führt `bash backend/mvnw -f backend/pom.xml verify` aus,
  einschließlich des PostgreSQL-Tests mit Testcontainers (in der CI nie
  übersprungen). Frontend-CI führt `npm ci`, Lint, Format-Check, Vitest und den
  Build mit Typprüfung aus. Pull Requests bauen und starten zusätzlich das
  Backend-Image. Deployments laufen nur nach grüner CI.
- Nur tatsächlich ausgeführte Tests und verifizierte Architektur- oder
  Deployment-Garantien als erfolgreich melden. Ein neuer Test für einen Fehler
  sollte nachweislich ohne die Korrektur fehlschlagen.

Die gehostete Portfolio-API ist lesend öffentlich. Schreibende Aufrufe
erfordern einen der veröffentlichten Demo-Zugänge mit passender Rolle; Kunden
dürfen nur für eigene Policen handeln, und ein Rate-Limit begrenzt Missbrauch.
Weil die Zugangsdaten öffentlich sind, ist die Umgebung nur für synthetische
Daten geeignet. Das ist kein produktionsreifes Identitätsmodell.

## 8. Offene Entscheidungen und Prüffragen

Diese Punkte werden durch diesen Leitfaden nicht stillschweigend entschieden:

- Ob die vorhandenen Fachmodule formell je einem DDD-Bounded-Context
  entsprechen und wie Aggregate- und Context-Maps aussehen.
- Das produktive Identitätsmodell (z. B. OIDC); die Demo-Zugänge dürfen nicht
  als produktionssicher dargestellt werden.
- Ob das Frontend über Claim-Übersicht, Login und Claim-Statuswechsel
  hinauswächst und dann Routing oder eine Query-/Cache-Bibliothek benötigt.

Bei Änderungen prüfen:

1. Entspricht die Änderung diesem Leitfaden und den angenommenen ADRs,
   insbesondere ADR 0003?
2. Stimmen Klassenplatzierung und Modulgrenzen mit der tatsächlichen Struktur
   überein?
3. Löst jede neue Abstraktion ein konkretes Problem, statt nur Indirektion
   hinzuzufügen?
4. Ist klar, welches Modul die Geschäftsregel besitzt und welcher Vertrag
   verwendet werden darf?
5. Sind Backend, Frontend, Tests, Dokumentation und Deployment-Auswirkungen
   berücksichtigt?
6. Werden Konflikte zwischen Code, ADRs und Dokumentation ausdrücklich
   benannt und einer Entscheidung überlassen, statt stillschweigend
   aufgelöst?
