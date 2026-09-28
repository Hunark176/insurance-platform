package de.hunar.insurance.claim.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "claims",
        indexes = {
                @Index(name = "idx_claim_policy_id", columnList = "policy_id"),
                @Index(name = "idx_claim_status", columnList = "status"),
                @Index(name = "idx_claim_created_at", columnList = "created_at")
        }
)
@Schema(description = "Ein Schadenfall im Versicherungssystem")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Eindeutige ID des Schadenfalls",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotNull
    @Column(
            name = "policy_id",
            nullable = false
    )
    @Schema(
            description = "ID der Versicherungspolice, zu der der Schadenfall gehört",
            example = "1"
    )
    private Long policyId;

    @NotNull
    @Column(name = "occurred_on", nullable = false)
    @Schema(description = "Datum des Schadenereignisses", example = "2026-09-20")
    private LocalDate occurredOn;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
            name = "claim_type",
            nullable = false,
            length = 30
    )
    @Schema(
            description = "Art des Schadenfalls",
            example = "AUTO",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ClaimType claimType;

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
            example = "Kratzer und Delle an der linken Fahrzeugtür",
            maxLength = 1000
    )
    private String description;

    @NotNull
    @DecimalMin(value = "0.01", message = "Der Schadenbetrag muss größer als null sein")
    @Column(
            name = "amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    @Schema(
            description = "Geschätzter Schadenbetrag in Euro",
            example = "1250.00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    @Schema(
            description = "Aktueller Bearbeitungsstatus des Schadenfalls",
            example = "RECEIVED",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    @Builder.Default
    private ClaimStatus status = ClaimStatus.RECEIVED;

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

    @Column(name = "updated_at")
    @Schema(
            description = "Zeitpunkt der letzten Änderung",
            example = "2026-09-25T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (status == null) {
            status = ClaimStatus.RECEIVED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}