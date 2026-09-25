package de.hunar.insurance.billing.domain;

import de.hunar.insurance.shared.domain.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "invoices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long policyId;
    @Column(nullable = false)
    private Long customerId;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    private Invoice(Long policyId, Long customerId, Money premium) {
        this.policyId = policyId;
        this.customerId = customerId;
        this.amount = premium.amount();
        this.currency = premium.currency().getCurrencyCode();
        this.status = InvoiceStatus.OPEN;
    }

    public static Invoice create(Long policyId, Long customerId, Money premium) {
        return new Invoice(policyId, customerId, premium);
    }
}
