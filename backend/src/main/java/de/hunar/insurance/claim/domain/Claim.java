package de.hunar.insurance.claim.domain;

import de.hunar.insurance.shared.domain.DomainException;
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
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a reported loss linked to an insurance policy.
 *
 * <p>Status changes are constrained by the claim lifecycle and concurrent updates
 * are detected through the optimistic-locking version.</p>
 */
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
            example = "2026-09-24T17:00:00Z",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    @Schema(
            description = "Zeitpunkt der letzten Änderung in UTC",
            example = "2026-09-25T10:30:00Z",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    /**
     * Creates a claim after enforcing invariants that must hold independently of request validation.
     *
     * @param policyId identifier of the policy covering the reported loss
     * @param occurredOn date on which the loss occurred
     * @param claimType category of the reported loss
     * @param description optional description, limited to 1,000 characters
     * @param amount reported amount, which must be greater than zero
     * @throws NullPointerException if a required value is {@code null}
     * @throws IllegalArgumentException if an identifier, amount, or description is invalid
     */
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

    /**
     * Creates a new claim in the initial {@link ClaimStatus#RECEIVED} state.
     *
     * @param policyId identifier of the associated policy
     * @param occurredOn date of the loss
     * @param claimType category of the loss
     * @param description optional loss description
     * @param amount reported loss amount
     * @return a new, unsaved claim
     * @throws NullPointerException if a required value is {@code null}
     * @throws IllegalArgumentException if an identifier or amount is invalid
     */
    public static Claim create(
            Long policyId,
            LocalDate occurredOn,
            ClaimType claimType,
            String description,
            BigDecimal amount
    ) {
        return new Claim(policyId, occurredOn, claimType, description, amount);
    }

    /**
     * Checks whether the current claim status can transition to the requested status.
     *
     * <p>The supported flow is {@code RECEIVED -> IN_REVIEW -> APPROVED|REJECTED};
     * terminal states cannot transition further.</p>
     *
     * @param target proposed next status
     * @throws DomainException if the transition is not permitted
     */
    public void assertCanTransitionTo(ClaimStatus target) {
        boolean valid = target != null && switch (status) {
            case RECEIVED -> target == ClaimStatus.IN_REVIEW;
            case IN_REVIEW -> target == ClaimStatus.APPROVED || target == ClaimStatus.REJECTED;
            case APPROVED, REJECTED -> false;
        };
        if (!valid) {
            throw new DomainException("Ungültiger Status-Übergang: " + status + " → " + target);
        }
    }

    /**
     * Applies a permitted status transition other than approval.
     *
     * <p>Approval has additional coverage rules and must go through
     * {@link #approve(BigDecimal, boolean)}, so they cannot be bypassed.</p>
     *
     * @param target next status in the claim lifecycle
     * @throws DomainException if the transition is not permitted or {@code target} is APPROVED
     */
    public void transitionTo(ClaimStatus target) {
        if (target == ClaimStatus.APPROVED) {
            throw new DomainException("Claims must be approved via approve() so coverage rules are checked");
        }
        assertCanTransitionTo(target);
        status = target;
    }

    /**
     * Approves the claim after checking the coverage rules of the underlying policy.
     *
     * <p>The policy data is supplied by the caller because it belongs to the policy module;
     * the rules themselves are enforced here so every approval path applies them.</p>
     *
     * @param coverageLimit maximum amount covered by the policy
     * @param coveredOnLossDate whether the policy was active on {@link #getOccurredOn()}
     * @throws DomainException if the claim is not in review, the policy did not cover the loss
     *                         date, or the amount exceeds the coverage limit
     */
    public void approve(BigDecimal coverageLimit, boolean coveredOnLossDate) {
        Objects.requireNonNull(coverageLimit, "coverageLimit must not be null");
        assertCanTransitionTo(ClaimStatus.APPROVED);
        if (!coveredOnLossDate) {
            throw new DomainException("Policy did not provide coverage on the date of loss");
        }
        if (amount.compareTo(coverageLimit) > 0) {
            throw new DomainException("Claim amount exceeds the policy coverage limit");
        }
        status = ClaimStatus.APPROVED;
    }
}
