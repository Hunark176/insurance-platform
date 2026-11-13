package de.hunar.insurance.product.dto;

import de.hunar.insurance.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Input for creating a product.
 *
 * @param code unique product code
 * @param name display name
 * @param coverageLimit maximum covered amount per loss
 * @param premium default premium
 * @param annualAggregateFactor optional annual aggregate as multiple of the coverage limit
 */
public record CreateProductRequest(@NotBlank String code, @NotBlank String name,
                                   @NotNull @DecimalMin("0.01") BigDecimal coverageLimit,
                                   @NotNull @DecimalMin("0.00") BigDecimal premium,
                                   @Schema(description = "Jahreshöchstleistung als Vielfaches der Deckungssumme "
                                           + "(z. B. 2 = alle Schäden eines Versicherungsjahres zusammen höchstens "
                                           + "2 × Deckungssumme). Leer = nur Deckungssumme je Schadenfall.",
                                           example = "2", nullable = true)
                                   @Min(1) @Max(Product.MAX_ANNUAL_AGGREGATE_FACTOR) Integer annualAggregateFactor) {
}
