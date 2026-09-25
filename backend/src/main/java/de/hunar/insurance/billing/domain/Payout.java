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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "payouts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_id", nullable = false, unique = true)
    private Long claimId;

    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayoutStatus status;

    private Payout(Long claimId, Long policyId, Money amount) {
        this.claimId = claimId;
        this.policyId = policyId;
        this.amount = amount.amount();
        this.currency = amount.currency().getCurrencyCode();
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
}
