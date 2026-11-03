package de.hunar.insurance.policy.events;

import de.hunar.insurance.shared.domain.Money;
import de.hunar.insurance.audit.AuditContext;

/**
 * Published after policy issuance so downstream modules can create an invoice.
 *
 * @param policyId newly issued policy identifier
 * @param customerId insured customer identifier
 * @param premium premium to invoice
 * @param auditContext actor and request metadata from the issuance request
 */
public record PolicyIssuedEvent(Long policyId, Long customerId, Money premium, AuditContext auditContext) {
    public PolicyIssuedEvent(Long policyId, Long customerId, Money premium) {
        this(policyId, customerId, premium, null);
    }
}
