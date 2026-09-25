package de.hunar.insurance.billing.dto;

import de.hunar.insurance.billing.domain.InvoiceStatus;

import java.math.BigDecimal;

public record InvoiceResponse(Long id, Long policyId, Long customerId, BigDecimal amount, String currency,
                              InvoiceStatus status) {
}
