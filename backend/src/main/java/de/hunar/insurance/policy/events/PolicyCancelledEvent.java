package de.hunar.insurance.policy.events;

import de.hunar.insurance.audit.AuditContext;
import de.hunar.insurance.shared.domain.Money;

import java.time.LocalDate;

/**
 * Published after a policy is cancelled so billing can credit the premium for the period that is
 * no longer covered.
 *
 * @param policyId cancelled policy
 * @param customerId insured customer
 * @param validFrom first day of the policy term
 * @param validTo last day of the policy term (inclusive)
 * @param cancelledOn last covered day
 * @param premium premium charged for the whole term
 * @param auditContext actor and request metadata of the cancellation
 */
public record PolicyCancelledEvent(
        Long policyId,
        Long customerId,
        LocalDate validFrom,
        LocalDate validTo,
        LocalDate cancelledOn,
        Money premium,
        AuditContext auditContext
) {
}
