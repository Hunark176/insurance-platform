package de.hunar.insurance.claim.events;

public record ClaimRejectedEvent(Long claimId, Long policyId) {
}
