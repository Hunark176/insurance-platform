package de.hunar.insurance.billing.domain;

import de.hunar.insurance.shared.domain.Money;
import jakarta.persistence.*;
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
@Table(name = "invoices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Positive
    @Column(nullable = false)
    private Long policyId;
    @NotNull
    @Positive
    @Column(nullable = false)
    private Long customerId;
    @NotNull
    @DecimalMin("0.00")
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
    private InvoiceStatus status;

    private Invoice(Long policyId, Long customerId, Money premium) {
        this.policyId = requirePositiveId(policyId, "policyId");
        this.customerId = requirePositiveId(customerId, "customerId");
        Money requiredPremium = Objects.requireNonNull(premium, "premium must not be null");
        this.amount = requiredPremium.amount();
        this.currency = requiredPremium.currency().getCurrencyCode();
        this.status = InvoiceStatus.OPEN;
    }

    public static Invoice create(Long policyId, Long customerId, Money premium) {
        return new Invoice(policyId, customerId, premium);
    }

    private static Long requirePositiveId(Long value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }
}
