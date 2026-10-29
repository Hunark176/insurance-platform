package de.hunar.insurance.policy.domain;

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
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PolicyStatus status;

    @Version
    @Column(nullable = false)
    private long version;

    public Policy(Long customerId, Long productId, LocalDate validFrom, LocalDate validTo,
                  BigDecimal coverageLimit, BigDecimal premium) {
        this.customerId = requirePositiveId(customerId, "customerId");
        this.productId = requirePositiveId(productId, "productId");
        this.validFrom = Objects.requireNonNull(validFrom, "validFrom must not be null");
        this.validTo = Objects.requireNonNull(validTo, "validTo must not be null");
        if (!validTo.isAfter(validFrom)) {
            throw new IllegalArgumentException("validTo must be after validFrom");
        }
        this.coverageLimit = requireAmount(coverageLimit, "coverageLimit", true);
        this.premium = requireAmount(premium, "premium", false);
        this.status = PolicyStatus.ACTIVE;
    }

    @AssertTrue(message = "validTo must be after validFrom")
    public boolean isValidTerm() {
        return validFrom != null && validTo != null && validTo.isAfter(validFrom);
    }

    public void cancel() {
        if (status != PolicyStatus.ACTIVE) {
            throw new IllegalStateException("Only an active policy can be cancelled");
        }
        status = PolicyStatus.CANCELLED;
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
