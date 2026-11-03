package de.hunar.insurance.claim.events;

import de.hunar.insurance.shared.domain.Money;
import de.hunar.insurance.audit.AuditContext;

/**
 * Published after a claim is approved so downstream modules can create a payout.
 *
 * @param claimId approved claim identifier
 * @param policyId policy associated with the claim
 * @param amount approved payout amount
 * @param auditContext actor and request metadata from the approval request
 */
public record ClaimApprovedEvent(Long claimId, Long policyId, Money amount, AuditContext auditContext) {
    public ClaimApprovedEvent(Long claimId, Long policyId, Money amount) {
        this(claimId, policyId, amount, null);
    }
}
