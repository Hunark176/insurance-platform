package de.hunar.insurance.policy.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "policies")
@Getter
@NoArgsConstructor
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;
    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;
    @Column(name = "coverage_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal coverageLimit;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal premium;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PolicyStatus status;

    public Policy(Long customerId, Long productId, LocalDate validFrom, LocalDate validTo,
                  BigDecimal coverageLimit, BigDecimal premium) {
        this.customerId = customerId;
        this.productId = productId;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.coverageLimit = coverageLimit;
        this.premium = premium;
        this.status = PolicyStatus.ACTIVE;
    }
    public void cancel() { this.status = PolicyStatus.CANCELLED; }
}
