package de.hunar.insurance.underwriting.domain;

public record UnderwritingDecision(Long customerId, Long productId, boolean eligible, String reason) {
}
