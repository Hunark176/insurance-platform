package de.hunar.insurance.claim.dto;

import de.hunar.insurance.claim.entity.ClaimStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Antwortdaten eines Schadenfalls")
public record ClaimResponse(

        @Schema(
                description = "Eindeutige ID des Claims",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Kundennummer",
                example = "K12345"
        )
        String customerNumber,

        @Schema(
                description = "Typ des Schadenfalls",
                example = "AUTO"
        )
        String claimType,

        @Schema(
                description = "Detaillierte Schadensbeschreibung",
                example = "Kratzer an der linken Fahrzeugtür"
        )
        String description,

        @Schema(
                description = "Geschätzter Schadenbetrag in Euro",
                example = "500.00"
        )
        BigDecimal amount,

        @Schema(
                description = "Aktueller Bearbeitungsstatus",
                example = "RECEIVED"
        )
        ClaimStatus status,

        @Schema(
                description = "Zeitpunkt der Erstellung",
                example = "2026-09-24T17:00:00"
        )
        LocalDateTime createdAt
) {
}