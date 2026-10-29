package de.hunar.insurance.policy;

import de.hunar.insurance.policy.domain.Policy;
import de.hunar.insurance.policy.domain.PolicyStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyDomainTest {
    @Test
    void newlyIssuedPolicyIsActiveForItsTerm() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        Policy policy = new Policy(1L, 2L, start, start.plusYears(1),
                new BigDecimal("100000.00"), new BigDecimal("49.90"));

        assertThat(policy.getStatus()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(policy.getValidFrom()).isEqualTo(start);
        assertThat(policy.getValidTo()).isAfter(policy.getValidFrom());
    }

    @Test
    void cancellingPolicyChangesStatus() {
        Policy policy = new Policy(1L, 2L, LocalDate.now(), LocalDate.now().plusDays(1),
                BigDecimal.TEN, BigDecimal.ONE);

        policy.cancel();

        assertThat(policy.getStatus()).isEqualTo(PolicyStatus.CANCELLED);
    }

    @Test
    void cancelledPolicyCannotBeCancelledAgain() {
        Policy policy = newPolicy();
        policy.cancel();

        assertThatThrownBy(policy::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only an active policy");
    }

    @Test
    void policyRejectsInvalidTermAndAmounts() {
        LocalDate start = LocalDate.of(2026, 1, 1);

        assertThatThrownBy(() -> new Policy(1L, 2L, start, start,
                BigDecimal.TEN, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("validTo");
        assertThatThrownBy(() -> new Policy(1L, 2L, start, start.plusDays(1),
                BigDecimal.ZERO, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("coverageLimit");
    }

    private Policy newPolicy() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        return new Policy(1L, 2L, start, start.plusYears(1),
                new BigDecimal("100000.00"), new BigDecimal("49.90"));
    }
}
