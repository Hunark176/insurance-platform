package de.hunar.insurance.policy;

import de.hunar.insurance.policy.domain.Policy;
import de.hunar.insurance.policy.domain.PolicyStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

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
}
