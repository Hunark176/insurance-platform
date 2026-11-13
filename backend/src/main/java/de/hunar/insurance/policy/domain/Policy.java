package de.hunar.insurance.policy.domain;

import de.hunar.insurance.policy.PolicyYear;
import de.hunar.insurance.shared.domain.DomainException;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents coverage issued to a customer for a product over a fixed date range.
 *
 * <p>The term is exclusive at the end: {@code validTo} must be after {@code validFrom}.
 * Updates are protected from lost writes by optimistic locking.</p>
 */
@Entity
@Table(name = "policies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Positive
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    @NotNull
    @Positive
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @NotNull
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;
    @NotNull
    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;
    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 17, fraction = 2)
    @Column(name = "coverage_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal coverageLimit;
    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 17, fraction = 2)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal premium;

    /**
     * Maximum total paid for all losses of one policy year, copied from the product terms at
     * issuance like the coverage limit; {@code null} means only the per-loss limit applies.
     */
    @Digits(integer = 17, fraction = 2)
    @Column(name = "annual_aggregate_limit", precision = 19, scale = 2)
    private BigDecimal annualAggregateLimit;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PolicyStatus status;

    /**
     * Last covered day of a cancelled policy; {@code null} while the policy is not cancelled.
     */
    @Column(name = "cancelled_on")
    private LocalDate cancelledOn;

    @Version
    @Column(nullable = false)
    private long version;

    /**
     * Creates an active policy without an annual aggregate.
     *
     * @see #Policy(Long, Long, LocalDate, LocalDate, BigDecimal, BigDecimal, BigDecimal)
     */
    public Policy(Long customerId, Long productId, LocalDate validFrom, LocalDate validTo,
                  BigDecimal coverageLimit, BigDecimal premium) {
        this(customerId, productId, validFrom, validTo, coverageLimit, premium, null);
    }

    /**
     * Creates a policy in the active state.
     *
     * @param customerId customer receiving the coverage
     * @param productId product whose terms are copied to the policy
     * @param validFrom first date of the policy term
     * @param validTo last date of the policy term (inclusive)
     * @param coverageLimit maximum covered amount per loss
     * @param premium policy premium
     * @param annualAggregateLimit maximum total per policy year, or {@code null}
     * @throws NullPointerException if a required value is {@code null}
     * @throws IllegalArgumentException if identifiers, dates, or monetary values are invalid, or
     *                                  the annual aggregate is below the per-loss limit
     */
    public Policy(Long customerId, Long productId, LocalDate validFrom, LocalDate validTo,
                  BigDecimal coverageLimit, BigDecimal premium, BigDecimal annualAggregateLimit) {
        this.customerId = requirePositiveId(customerId, "customerId");
        this.productId = requirePositiveId(productId, "productId");
        this.validFrom = Objects.requireNonNull(validFrom, "validFrom must not be null");
        this.validTo = Objects.requireNonNull(validTo, "validTo must not be null");
        if (!validTo.isAfter(validFrom)) {
            throw new IllegalArgumentException("validTo must be after validFrom");
        }
        this.coverageLimit = requireAmount(coverageLimit, "coverageLimit", true);
        this.premium = requireAmount(premium, "premium", false);
        if (annualAggregateLimit != null) {
            requireAmount(annualAggregateLimit, "annualAggregateLimit", true);
            if (annualAggregateLimit.compareTo(this.coverageLimit) < 0) {
                throw new IllegalArgumentException("annualAggregateLimit must not be below the coverage limit");
            }
        }
        this.annualAggregateLimit = annualAggregateLimit;
        this.status = PolicyStatus.ACTIVE;
    }

    /**
     * Returns the policy year that contains the given date. Policy years start on the
     * anniversary of {@link #getValidFrom()}; a leap-day start falls back to 28 February.
     *
     * @param date date within or after the start of the term
     * @return first and last day (inclusive) of that policy year
     * @throws IllegalArgumentException if the date lies before the start of the term
     */
    public PolicyYear policyYearOf(LocalDate date) {
        return PolicyYear.containing(validFrom, date);
    }

    /**
     * Reports whether this policy has a valid, non-empty coverage term.
     *
     * @return {@code true} when both dates are present and {@code validTo} is after {@code validFrom}
     */
    @AssertTrue(message = "validTo must be after validFrom")
    public boolean isValidTerm() {
        return validFrom != null && validTo != null && validTo.isAfter(validFrom);
    }

    /**
     * Cancels an active policy with effect from the end of {@code effectiveOn}.
     *
     * <p>Follows the loss-occurrence principle: losses that occurred up to and including
     * {@code effectiveOn} remain covered and can still be reported and settled after the
     * cancellation; later losses are not covered. A cancellation therefore never removes
     * coverage retroactively. Cancelling before the term starts means the policy never
     * provides coverage.</p>
     *
     * @param effectiveOn last covered day
     * @param today current date, used to reject retroactive cancellations
     * @throws DomainException if the policy is not active, the date lies in the past or after
     *                         the end of the term
     */
    public void cancel(LocalDate effectiveOn, LocalDate today) {
        Objects.requireNonNull(effectiveOn, "effectiveOn must not be null");
        Objects.requireNonNull(today, "today must not be null");
        if (status != PolicyStatus.ACTIVE) {
            throw new DomainException("Only an active policy can be cancelled");
        }
        if (effectiveOn.isBefore(today)) {
            throw new DomainException("A policy cannot be cancelled retroactively");
        }
        if (effectiveOn.isAfter(validTo)) {
            throw new DomainException("The cancellation date must not be after the end of the policy term");
        }
        status = PolicyStatus.CANCELLED;
        cancelledOn = effectiveOn;
    }

    /**
     * Returns the last day on which losses are covered: the end of the term, or the
     * cancellation date if the policy was cancelled earlier.
     *
     * @return last covered day (inclusive)
     */
    public LocalDate getCoverageEndsOn() {
        return cancelledOn != null && cancelledOn.isBefore(validTo) ? cancelledOn : validTo;
    }

    /**
     * Checks whether a loss on the given date is covered (loss-occurrence principle).
     *
     * <p>Only the date matters, not the current status: a cancelled or expired policy still
     * covers losses that occurred while it was in force.</p>
     *
     * @param lossDate date on which the loss occurred
     * @return {@code true} if the date lies within the coverage period, both ends inclusive
     */
    public boolean covers(LocalDate lossDate) {
        Objects.requireNonNull(lossDate, "lossDate must not be null");
        return !lossDate.isBefore(validFrom) && !lossDate.isAfter(getCoverageEndsOn());
    }

    private static Long requirePositiveId(Long value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }

    private static BigDecimal requireAmount(BigDecimal value, String field, boolean mustBePositive) {
        if (value == null || (mustBePositive ? value.signum() <= 0 : value.signum() < 0)
                || value.scale() > 2 || value.precision() - value.scale() > 17) {
            throw new IllegalArgumentException(field + " must fit a decimal with at most 2 decimal places");
        }
        return value;
    }
}
