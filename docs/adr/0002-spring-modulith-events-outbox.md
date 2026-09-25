# ADR 0002: Spring Modulith Events

Domänenereignisse werden mit Spring Modulith veröffentlicht. Listener sind
idempotent und erzeugen keine Rückaufrufe in das sendende Modul. Für eine
produktive Outbox-Auslieferung kann der Modulith-Event-Publikationsmechanismus
mit einer relationalen Event-Publikation ergänzt werden.
