package de.hunar.insurance.policy.dto;

import de.hunar.insurance.policy.domain.PolicyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyResponse(Long id, Long customerId, Long productId, LocalDate validFrom, LocalDate validTo,
                             BigDecimal coverageLimit, BigDecimal premium, PolicyStatus status) {
}
