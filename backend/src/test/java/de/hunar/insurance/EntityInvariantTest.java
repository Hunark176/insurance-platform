package de.hunar.insurance;

import de.hunar.insurance.billing.domain.Invoice;
import de.hunar.insurance.billing.domain.InvoiceStatus;
import de.hunar.insurance.billing.domain.Payout;
import de.hunar.insurance.billing.domain.PayoutStatus;
import de.hunar.insurance.claim.domain.Claim;
import de.hunar.insurance.claim.domain.ClaimType;
import de.hunar.insurance.customer.domain.Customer;
import de.hunar.insurance.policy.domain.Policy;
import de.hunar.insurance.policy.domain.PolicyStatus;
import de.hunar.insurance.product.domain.Product;
import de.hunar.insurance.shared.domain.Money;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityInvariantTest {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void validEntitiesSatisfyTheirPersistenceConstraints() {
        LocalDate validFrom = LocalDate.of(2026, 1, 1);
        assertThat(validator.validate(new Customer("C-1", "Customer", "customer@example.test"))).isEmpty();
        assertThat(validator.validate(new Product("P-1", "Product",
                new BigDecimal("1000.00"), new BigDecimal("10.00")))).isEmpty();
        assertThat(validator.validate(new Policy(1L, 1L, validFrom, validFrom.plusYears(1),
                new BigDecimal("1000.00"), new BigDecimal("10.00")))).isEmpty();
        assertThat(validator.validate(Claim.create(1L, validFrom, ClaimType.AUTO,
                "Damage", new BigDecimal("10.00")))).isEmpty();
        assertThat(validator.validate(Invoice.create(1L, 1L, Money.eur(BigDecimal.ZERO)))).isEmpty();
        assertThat(validator.validate(Payout.create(1L, 1L, Money.eur(new BigDecimal("10.00"))))).isEmpty();
    }

    @Test
    void customerEmailIsValidatedWhenTheEntityIsPersisted() {
        Customer customer = new Customer("C-1", "Customer", "not-an-email");

        assertThat(validator.validate(customer))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("email");
    }

    @Test
    void constructorsRejectInvalidRequiredValuesAndAmounts() {
        assertThatThrownBy(() -> new Customer(" ", "Customer", "customer@example.test"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Product("P-1", "Product", BigDecimal.ZERO, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("coverageLimit");
        assertThatThrownBy(() -> new Product("P-1", "Product", BigDecimal.ONE,
                new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("premium");
        assertThatThrownBy(() -> Invoice.create(0L, 1L, Money.eur(BigDecimal.ONE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("policyId");
        assertThatThrownBy(() -> Payout.create(1L, 1L, Money.eur(BigDecimal.ZERO)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
    }

    @Test
    void paymentStateChangesAreOneWay() {
        Payout payout = Payout.create(1L, 1L, Money.eur(new BigDecimal("10.00")));

        payout.markPaid();

        assertThat(payout.getStatus()).isEqualTo(PayoutStatus.EXECUTED);
        assertThatThrownBy(payout::markPaid).isInstanceOf(IllegalStateException.class);
        assertThat(Invoice.create(1L, 1L, Money.eur(BigDecimal.ZERO)).getStatus())
                .isEqualTo(InvoiceStatus.OPEN);
        assertThat(new Policy(1L, 1L, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1),
                BigDecimal.ONE, BigDecimal.ZERO).getStatus()).isEqualTo(PolicyStatus.ACTIVE);
    }
}
