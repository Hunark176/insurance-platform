package de.hunar.insurance.billing.dto;

import de.hunar.insurance.billing.domain.PayoutStatus;

import java.math.BigDecimal;

public record PayoutResponse(Long id, Long claimId, Long policyId, BigDecimal amount, String currency,
                             PayoutStatus status) {
}
