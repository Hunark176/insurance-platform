package de.hunar.insurance.policy.dto;

import de.hunar.insurance.policy.domain.PolicyStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Policy with readable customer and product names.
 *
 * @param cancelledOn last covered day of a cancelled policy, otherwise {@code null}
 * @param annualAggregateLimit maximum paid per policy year, {@code null} if only the per-loss limit applies
 */
public record PolicyResponse(Long id, Long customerId, String customerNumber, String customerName,
                             Long productId, String productName, LocalDate validFrom, LocalDate validTo,
                             BigDecimal coverageLimit, BigDecimal premium, PolicyStatus status,
                             @Schema(types = {"string", "null"}, format = "date",
                                     description = "Letzter gedeckter Tag einer stornierten Police")
                             LocalDate cancelledOn,
                             @Schema(types = {"number", "null"}, description = "Jahreshöchstleistung je Versicherungsjahr")
                             BigDecimal annualAggregateLimit) {
}
