package de.hunar.insurance.policy;

import de.hunar.insurance.shared.domain.Money;

import java.time.LocalDate;

/**
 * Immutable policy information shared with another module.
 *
 * @param id persistent policy identifier
 * @param customerId identifier of the insured customer
 * @param customerNumber customer reference suitable for display or validation
 * @param customerName customer display name
 * @param productId identifier of the issued product
 * @param validFrom first date of the policy term
 * @param validTo exclusive end date of the policy term
 * @param coverageLimit maximum amount covered by the policy
 */
public record PolicySnapshot(
        Long id,
        Long customerId,
        String customerNumber,
        String customerName,
        Long productId,
        LocalDate validFrom,
        LocalDate validTo,
        Money coverageLimit
) {
}
