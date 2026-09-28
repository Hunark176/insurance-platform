package de.hunar.insurance.claim.dto;

import de.hunar.insurance.claim.entity.ClaimType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Daten zum Anlegen eines neuen Schadenfalls")
public record CreateClaimRequest(

        @NotNull(message = "Policen-ID ist erforderlich")
        Long policyId,

        @NotNull(message = "Schadendatum ist erforderlich")
        @PastOrPresent(message = "Das Schadendatum darf nicht in der Zukunft liegen")
        LocalDate occurredOn,

        @NotBlank(message = "Kundennummer darf nicht leer sein")
        @Size(max = 50, message = "Kundennummer darf maximal 50 Zeichen enthalten")
        @Schema(
                description = "Kundennummer",
                example = "K12345",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String customerNumber,

        @NotNull(message = "Claim-Typ ist erforderlich")
        ClaimType claimType,

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
        @DecimalMin(value = "0.01", message = "Der Schadenbetrag muss größer als null sein")
        @Schema(
                description = "Geschätzter Schadenbetrag in Euro",
                example = "500.00",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        BigDecimal amount
) {
}