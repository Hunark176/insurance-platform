# ADR 0003: Persistenzzugriff im Fachmodul

- **Status:** Angenommen
- **Entschieden am:** 28. Oktober 2026
- **Geltungsbereich:** Backend-Fachmodule

## Kontext

Die ursprüngliche Fassung dieser Entscheidung sah vor, Domänenmodelle von JPA
zu trennen und Spring-Data-Repositories hinter Ports und Adaptern in
`infrastructure/persistence` zu kapseln. Diese zusätzliche Schichtung wurde
bei der Vereinheitlichung der Module nicht beibehalten: Sie erzeugte
Weiterleitungsabstraktionen ohne eigene fachliche oder technische Aufgabe.

Das Backend ist ein modularer Monolith. Die Fachmodule kontrollieren ihre
internen Klassen und veröffentlichen für modulübergreifende Aufrufe
explizite APIs und Ereignisse.

## Entscheidung

Fachmodule verwenden ihre Spring-Data-Repositories direkt im `domain`-Paket.
JPA-Entities und Repository-Interfaces sind Teil des jeweiligen Moduls;
Anwendungsservices in `application` koordinieren Anwendungsfälle und
Fachregeln.

Repository-Ports und zusätzliche Adapter werden nicht allein zur Umsetzung
eines Architekturbegriffs angelegt. Eine solche Trennung ist gerechtfertigt,
wenn ein konkreter Bedarf entsteht, zum Beispiel ein unabhängiges
Domänenmodell, mehrere Persistenzimplementierungen oder eine externe technische
Grenze. Modulübergreifender Zugriff auf interne Repositories bleibt unzulässig;
andere Module verwenden die veröffentlichten Modul-APIs.

Diese Entscheidung ersetzt die ursprüngliche Zielrichtung der ersten Fassung
dieses ADRs.

## Konsequenzen

- Fachmodule haben ein einheitliches, direkt nachvollziehbares Muster mit
  `application`, `domain`, `controller` und `dto`, sofern die jeweilige
  Funktion diese Pakete benötigt.
- Es gibt keine ungenutzten Repository-Ports oder reinen
  Weiterleitungsadapter.
- Domain- und Anwendungscode ist an Spring Data/JPA gekoppelt. Eine spätere
  Ablösung oder Isolierung der Persistenz erfordert eine bewusste Änderung
  dieses ADRs und passenden Tests.
- Spring-Modulith-Architekturtests schützen weiterhin die Modulgrenzen.
