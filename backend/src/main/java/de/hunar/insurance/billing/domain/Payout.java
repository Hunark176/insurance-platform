package de.hunar.insurance.billing.domain;

import de.hunar.insurance.shared.domain.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "payouts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive
    @Column(name = "claim_id", nullable = false, unique = true)
    private Long claimId;

    @NotNull
    @Positive
    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 17, fraction = 2)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    @Column(nullable = false, length = 3)
    private String currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayoutStatus status;

    @Version
    @Column(nullable = false)
    private long version;

    private Payout(Long claimId, Long policyId, Money amount) {
        this.claimId = requirePositiveId(claimId, "claimId");
        this.policyId = requirePositiveId(policyId, "policyId");
        Money requiredAmount = Objects.requireNonNull(amount, "amount must not be null");
        if (requiredAmount.amount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        this.amount = requiredAmount.amount();
        this.currency = requiredAmount.currency().getCurrencyCode();
        this.status = PayoutStatus.CREATED;
    }

    public static Payout create(Long claimId, Long policyId, Money amount) {
        return new Payout(claimId, policyId, amount);
    }

    public void markPaid() {
        if (status != PayoutStatus.CREATED) {
            throw new IllegalStateException("Only a newly created payout can be paid");
        }
        status = PayoutStatus.EXECUTED;
    }

    private static Long requirePositiveId(Long value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }
}
