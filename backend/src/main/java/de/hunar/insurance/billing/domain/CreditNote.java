package de.hunar.insurance.billing.domain;

import de.hunar.insurance.shared.domain.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

/**
 * Credit for the premium of the period no longer covered after a policy cancellation.
 *
 * <p>The premium covers the whole term {@code validFrom..validTo} (both inclusive). After a
 * cancellation only the days up to and including {@code cancelledOn} are owed (§ 39 VVG), so the
 * remaining days are credited pro rata per day, rounded half-up to cents. At most one credit note
 * exists per policy, because a policy can be cancelled only once.</p>
 */
@Getter
@Entity
@Table(name = "credit_notes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_id", nullable = false, unique = true)
    private Long policyId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    /** Number of term days credited and total term days, for traceability. */
    @Column(name = "credited_days", nullable = false)
    private long creditedDays;

    @Column(name = "term_days", nullable = false)
    private long termDays;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    private CreditNote(Long policyId, Long customerId, Money amount, long creditedDays, long termDays,
                       Instant createdAt) {
        this.policyId = Objects.requireNonNull(policyId);
        this.customerId = Objects.requireNonNull(customerId);
        this.amount = amount.amount();
        this.currency = amount.currency().getCurrencyCode();
        this.creditedDays = creditedDays;
        this.termDays = termDays;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    /**
     * Calculates the credit for a cancellation.
     *
     * @param premium premium charged for the whole term
     * @param validFrom first day of the term
     * @param validTo last day of the term (inclusive)
     * @param cancelledOn last covered day; before {@code validFrom} means the policy never started
     * @return the credit note, or empty if nothing is to be credited (cancelled on the last day)
     */
    public static Optional<CreditNote> forCancellation(Long policyId, Long customerId, Money premium,
                                                       LocalDate validFrom, LocalDate validTo, LocalDate cancelledOn,
                                                       Instant now) {
        long termDays = ChronoUnit.DAYS.between(validFrom, validTo) + 1;
        LocalDate firstUncoveredDay = cancelledOn.isBefore(validFrom) ? validFrom : cancelledOn.plusDays(1);
        long creditedDays = Math.max(0, ChronoUnit.DAYS.between(firstUncoveredDay, validTo) + 1);
        if (creditedDays == 0 || premium.amount().signum() == 0) {
            return Optional.empty();
        }
        BigDecimal credit = premium.amount()
                .multiply(BigDecimal.valueOf(creditedDays))
                .divide(BigDecimal.valueOf(termDays), 2, RoundingMode.HALF_UP);
        return Optional.of(new CreditNote(policyId, customerId, new Money(credit, premium.currency()),
                creditedDays, termDays, now));
    }
}
