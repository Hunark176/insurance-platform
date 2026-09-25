# ADR 0001: Modularer Monolith

Wir starten als modularer Monolith statt als verteilte Microservices. Dadurch
bleiben Transaktionen und lokale Entwicklung einfach, während Spring Modulith
die fachlichen Grenzen und Abhängigkeiten erzwingt. Eine spätere Extraktion
einzelner Module bleibt möglich, weil Domänen über APIs und Events gekoppelt
sind.
