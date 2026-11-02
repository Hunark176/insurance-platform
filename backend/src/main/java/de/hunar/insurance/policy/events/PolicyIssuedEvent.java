package de.hunar.insurance.policy.events;

import de.hunar.insurance.shared.domain.Money;
import de.hunar.insurance.audit.AuditContext;

public record PolicyIssuedEvent(Long policyId, Long customerId, Money premium, AuditContext auditContext) {
    public PolicyIssuedEvent(Long policyId, Long customerId, Money premium) {
        this(policyId, customerId, premium, null);
    }
}
