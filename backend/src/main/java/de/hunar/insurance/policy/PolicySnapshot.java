package de.hunar.insurance.policy;

import de.hunar.insurance.shared.domain.Money;

import java.time.LocalDate;

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
