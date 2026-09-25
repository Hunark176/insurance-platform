package de.hunar.insurance.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProductRequest(@NotBlank String code, @NotBlank String name,
                                   @NotNull @DecimalMin("0.01") BigDecimal coverageLimit,
                                   @NotNull @DecimalMin("0.00") BigDecimal premium) {
}
