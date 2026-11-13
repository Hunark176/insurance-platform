# Dokumentation

Architektur- und API-Dokumentation der Insurance Platform. Die Markdown-Dateien
sind die einzige gepflegte Fassung; der Code ist bei Abweichungen maßgeblich.

## Einstieg

- [Architektur-Kurzüberblick](architecture.md)
- [Technische Architektur- und Schnittstellendokumentation](architecture/technical-documentation.md) – Module, Schnittstellen, Fachregeln, Sicherheit, Betrieb
- [API-Dokumentation](api/README.md) – Zugänge, Fehler, wichtige Endpunkte

## Arbeitsgrundlagen

- [Architekturprinzipien und Klassenplatzierung](architecture/architecture-principles.md) – wohin neuer Code gehört und welche Abhängigkeiten erlaubt sind
- [Domänen- und Modulüberblick mit Glossar](architecture/domain-overview.md)
- [Architektur-Roadmap und offene Punkte](architecture/architecture-roadmap.md)

## Architekturentscheidungen (ADRs)

| ADR | Entscheidung |
| --- | ------------ |
| [0001](adr/0001-modularer-monolith.md) | Modularer Monolith statt Microservices |
| [0002](adr/0002-spring-modulith-events-outbox.md) | Spring-Modulith-Events mit persistenter Outbox |
| [0003](adr/0003-persistenzzugriff-im-domain-modul.md) | Spring-Data-Repositories direkt im Fachmodul |
| [0004](adr/0004-claim-policy-migration.md) | Bestehende Claims sicher an Policen anbinden |
| [0005](adr/0005-deckungsregeln.md) | Deckungsregeln: Schadenereignisprinzip und Jahreshöchstleistung |

Neue ADRs bekommen die nächste freie Nummer und werden hier eingetragen.

## Weiteres

- [Diagramme](architecture/diagrams) (SVG, in der technischen Dokumentation eingebettet)
- [Screenshots](screenshots)
