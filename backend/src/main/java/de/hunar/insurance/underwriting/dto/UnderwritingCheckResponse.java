package de.hunar.insurance.underwriting.dto;

public record UnderwritingCheckResponse(Long customerId, Long productId, boolean eligible, String reason) {
}
