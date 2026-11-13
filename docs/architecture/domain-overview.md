# Domain and module overview

The backend is organized by business capability below
`de.hunar.insurance`; these are implemented Spring Modulith modules, not merely
reserved folders. The code includes customer, product, underwriting, policy,
claim, billing, audit, and shared modules.

`claim` reports losses linked to policies, checks that customers only report
losses for their own policies, validates coverage, and manages the claim
lifecycle; the `Claim` aggregate decides on approval. `policy` issues and
cancels policies, determines coverage by loss date and policy years, and
publishes `PolicyIssuedEvent`. `billing` consumes policy-issued and
claim-approved events (delivered through the outbox) to create exactly one
invoice per policy and one payout per approved claim. Customer and product data
are exposed to collaborating modules through public APIs and summary types;
underwriting currently checks customer existence and product activity. Audit
records are written through the audit module contract.

## Glossary

| Code term                 | German term                    | Meaning in this system                                                                 |
| ------------------------- | ------------------------------ | -------------------------------------------------------------------------------------- |
| Policy                    | Police, Versicherungsvertrag   | Contract of a customer for a product with a term (`validFrom`–`validTo`, inclusive)     |
| Claim                     | Schadenfall, Schadenmeldung    | Reported loss on a policy with loss date (`occurredOn`) and amount                      |
| Coverage limit            | Deckungssumme                  | Maximum paid per loss                                                                  |
| Annual aggregate limit    | Jahreshöchstleistung, „2-fache Maximierung“ | Maximum paid for all losses of one policy year; optional, copied from the product |
| Policy year               | Versicherungsjahr              | Period between two anniversaries of `validFrom` (`PolicyYear`)                          |
| Loss-occurrence principle | Schadenereignisprinzip         | Coverage depends on the loss date, not on the processing date ([ADR 0005](../adr/0005-deckungsregeln.md)) |
| Cancellation date         | Wirksamkeitsdatum des Stornos  | Last covered day of a cancelled policy (`cancelledOn`); never retroactive               |
| Underwriting              | Risikoprüfung                  | Decision whether a policy may be issued                                                 |
| Invoice / Payout          | Rechnung / Auszahlung          | Billing results of policy issuance and claim approval                                   |
| Credit note               | Gutschrift                     | Pro-rata premium for the days after a cancellation (§ 39 VVG)                           |

This is a current-code overview, not a complete DDD bounded-context map.
Aggregate ownership, ubiquitous-language definitions beyond this glossary, and
the future business context map remain open design work. For placement and
dependency rules, see
[Architekturprinzipien und Klassenplatzierung](architecture-principles.md).
