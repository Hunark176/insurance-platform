package de.hunar.insurance.policy;

import de.hunar.insurance.shared.domain.Money;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Immutable policy information shared with another module.
 *
 * @param id persistent policy identifier
 * @param customerId identifier of the insured customer
 * @param customerNumber customer reference suitable for display or validation
 * @param customerName customer display name
 * @param productId identifier of the issued product
 * @param validFrom first date of the policy term
 * @param validTo last date of the policy term (inclusive)
 * @param coverageLimit maximum amount covered per loss
 * @param annualAggregateLimit maximum total for all losses of one policy year, or {@code null}
 *                             if only the per-loss limit applies
 */
public record PolicySnapshot(
        Long id,
        Long customerId,
        String customerNumber,
        String customerName,
        Long productId,
        LocalDate validFrom,
        LocalDate validTo,
        Money coverageLimit,
        Money annualAggregateLimit
) {
    /**
     * @return the annual aggregate limit, if the policy has one
     */
    public Optional<Money> annualAggregate() {
        return Optional.ofNullable(annualAggregateLimit);
    }

    /**
     * @param date date within or after the start of the term, typically a loss date
     * @return the policy year containing the date
     */
    public PolicyYear policyYearOf(LocalDate date) {
        return PolicyYear.containing(validFrom, date);
    }
}
