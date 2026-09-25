package de.hunar.insurance.claim.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "claims")
@Schema(description = "Ein Schadenfall im System")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Eindeutige ID des Claims",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(
            name = "customer_number",
            nullable = false,
            length = 50
    )
    @Schema(
            description = "Kundennummer",
            example = "K12345",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String customerNumber;

    @NotBlank
    @Size(max = 100)
    @Column(
            name = "claim_type",
            nullable = false,
            length = 100
    )
    @Schema(
            description = "Typ des Claims, zum Beispiel Auto, Haus oder Leben",
            example = "Auto",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String claimType;

    @Size(
            max = 1000,
            message = "Die Beschreibung darf maximal 1000 Zeichen enthalten"
    )
    @Column(
            name = "description",
            length = 1000
    )
    @Schema(
            description = "Detaillierte Beschreibung des Schadens",
            example = "Kratzer am Lack",
            maxLength = 1000
    )
    private String description;

    @NotNull
    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Der Schadenbetrag darf nicht negativ sein"
    )
    @Column(
            name = "amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    @Schema(
            description = "Schadenbetrag in Euro",
            example = "500.00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    @Schema(
            description = "Aktueller Bearbeitungsstatus",
            example = "RECEIVED",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private ClaimStatus status;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    @Schema(
            description = "Zeitpunkt der Erstellung",
            example = "2026-09-24T17:00:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = ClaimStatus.RECEIVED;
        }
    }
}