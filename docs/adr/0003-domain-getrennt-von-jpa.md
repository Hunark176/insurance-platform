# ADR 0003: Domain getrennt von JPA

Neue Module verwenden Ports und Adapter: Domänenobjekte und Repository-Ports
bleiben fachlich, während Spring-Data-Implementierungen in
`infrastructure/persistence` liegen. Der bestehende Claim-Prototyp wird
schrittweise in dieses Muster überführt, ohne die laufende API zu brechen.
