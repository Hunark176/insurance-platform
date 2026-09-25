package de.hunar.insurance.claim.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Daten zum Anlegen eines neuen Schadenfalls")
public record CreateClaimRequest(

        Long policyId,

        @NotBlank(message = "Kundennummer darf nicht leer sein")
        @Size(max = 50, message = "Kundennummer darf maximal 50 Zeichen enthalten")
        @Schema(
                description = "Kundennummer",
                example = "K12345",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String customerNumber,

        @NotBlank(message = "Claim-Typ darf nicht leer sein")
        @Size(max = 100, message = "Claim-Typ darf maximal 100 Zeichen enthalten")
        @Schema(
                description = "Typ des Schadenfalls",
                example = "AUTO",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String claimType,

        @Size(
                max = 1000,
                message = "Die Beschreibung darf maximal 1000 Zeichen enthalten"
        )
        @Schema(
                description = "Detaillierte Schadensbeschreibung",
                example = "Kratzer an der linken Fahrzeugtür"
        )
        String description,

        @NotNull(message = "Schadenbetrag ist erforderlich")
        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "Der Schadenbetrag darf nicht negativ sein"
        )
        @Schema(
                description = "Geschätzter Schadenbetrag in Euro",
                example = "500.00",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        BigDecimal amount
) {
}