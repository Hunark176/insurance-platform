package de.hunar.insurance.claim.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Schema(description = "Eindeutige ID des Claims", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank
    @Column(name = "customer_number", nullable = false)
    @Schema(description = "Kundennummer", example = "K12345", required = true)
    private String customerNumber;

    @NotBlank
    @Column(name = "claim_type", nullable = false)
    @Schema(description = "Typ des Claims (z.B. Auto, Haus, Leben)", example = "Auto", required = true)
    private String claimType;

    @Column(length = 1000)
    @Schema(description = "Detaillierte Beschreibung des Schadens", example = "Kratzer am Lack", maxLength = 1000)
    private String description;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    @Schema(description = "Schadenbetrag in Euro", example = "500.00", required = true)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Status des Claims", example = "RECEIVED", required = true)
    private ClaimStatus status;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "Erstellungsdatum des Claims", example = "2026-09-23T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ClaimStatus.RECEIVED;
        }
    }
}