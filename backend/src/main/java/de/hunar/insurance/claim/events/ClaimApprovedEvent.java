package de.hunar.insurance.claim.events;

import de.hunar.insurance.shared.domain.Money;
import de.hunar.insurance.audit.AuditContext;

public record ClaimApprovedEvent(Long claimId, Long policyId, Money amount, AuditContext auditContext) {
    public ClaimApprovedEvent(Long claimId, Long policyId, Money amount) {
        this(claimId, policyId, amount, null);
    }
}
