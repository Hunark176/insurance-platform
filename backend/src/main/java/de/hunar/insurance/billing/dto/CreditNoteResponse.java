package de.hunar.insurance.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Credit for the premium of the period no longer covered after a cancellation.
 *
 * @param creditedDays term days credited
 * @param termDays total term days
 */
public record CreditNoteResponse(Long id, Long policyId, Long customerId, BigDecimal amount, String currency,
                                 long creditedDays, long termDays, Instant createdAt) {
}
