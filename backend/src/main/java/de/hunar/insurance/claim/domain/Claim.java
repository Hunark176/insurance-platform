package de.hunar.insurance.claim.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
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
    @Positive
    @Column(name = "policy_id", nullable = false)
    @Schema(description = "ID der Versicherungspolice, zu der der Schadenfall gehört", example = "1")
    private Long policyId;

    @NotNull
    @Column(name = "occurred_on", nullable = false)
    @Schema(description = "Datum des Schadenereignisses", example = "2026-09-20")
    private LocalDate occurredOn;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", nullable = false, length = 30)
    @Schema(
            description = "Art des Schadenfalls",
            example = "AUTO",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ClaimType claimType;

    @Size(max = 1000, message = "Die Beschreibung darf maximal 1000 Zeichen enthalten")
    @Column(name = "description", length = 1000)
    @Schema(
            description = "Detaillierte Beschreibung des Schadens",
            example = "Kratzer und Delle an der linken Fahrzeugtür",
            maxLength = 1000
    )
    private String description;

    @NotNull
    @DecimalMin(value = "0.01", message = "Der Schadenbetrag muss größer als null sein")
    @Digits(integer = 10, fraction = 2)
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    @Schema(
            description = "Geschätzter Schadenbetrag in Euro",
            example = "1250.00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Schema(
            description = "Aktueller Bearbeitungsstatus des Schadenfalls",
            example = "RECEIVED",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private ClaimStatus status = ClaimStatus.RECEIVED;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(
            description = "Zeitpunkt der Erstellung in UTC",
            example = "2026-09-24T17:00:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    @Schema(
            description = "Zeitpunkt der letzten Änderung in UTC",
            example = "2026-09-25T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    private Claim(Long policyId, LocalDate occurredOn, ClaimType claimType, String description, BigDecimal amount) {
        this.policyId = Objects.requireNonNull(policyId, "policyId must not be null");
        this.occurredOn = Objects.requireNonNull(occurredOn, "occurredOn must not be null");
        this.claimType = Objects.requireNonNull(claimType, "claimType must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        if (policyId <= 0) {
            throw new IllegalArgumentException("policyId must be positive");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        if (description != null && description.length() > 1000) {
            throw new IllegalArgumentException("description must not exceed 1000 characters");
        }
        this.description = description;
    }

    public static Claim create(
            Long policyId,
            LocalDate occurredOn,
            ClaimType claimType,
            String description,
            BigDecimal amount
    ) {
        return new Claim(policyId, occurredOn, claimType, description, amount);
    }

    public void assertCanTransitionTo(ClaimStatus target) {
        boolean valid = target != null && switch (status) {
            case RECEIVED -> target == ClaimStatus.IN_REVIEW;
            case IN_REVIEW -> target == ClaimStatus.APPROVED || target == ClaimStatus.REJECTED;
            case APPROVED, REJECTED -> false;
        };
        if (!valid) {
            throw new IllegalStateException("Ungültiger Status-Übergang: " + status + " → " + target);
        }
    }

    public void transitionTo(ClaimStatus target) {
        assertCanTransitionTo(target);
        status = target;
    }
}
