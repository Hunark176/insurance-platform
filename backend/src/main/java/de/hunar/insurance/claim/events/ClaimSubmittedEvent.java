package de.hunar.insurance.claim.events;

public record ClaimSubmittedEvent(Long claimId, Long policyId) {
}
