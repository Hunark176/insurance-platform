package de.hunar.insurance.policy.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record IssuePolicyRequest(@NotNull Long customerId, @NotNull Long productId,
                                 @NotNull LocalDate validFrom, @NotNull @Future LocalDate validTo) {
}
