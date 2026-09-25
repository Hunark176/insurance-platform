package de.hunar.insurance.underwriting.dto;

import jakarta.validation.constraints.NotNull;

public record UnderwritingCheckRequest(@NotNull Long customerId, @NotNull Long productId) {
}
