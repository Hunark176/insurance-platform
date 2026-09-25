package de.hunar.insurance.claim.events;

import de.hunar.insurance.shared.domain.Money;

public record ClaimApprovedEvent(Long claimId, Long policyId, Money amount) {
}
