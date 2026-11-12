package de.hunar.insurance.policy;

import de.hunar.insurance.policy.domain.Policy;
import de.hunar.insurance.policy.domain.PolicyStatus;
import de.hunar.insurance.shared.domain.DomainException;
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

    private static final LocalDate TERM_START = LocalDate.of(2026, 1, 1);
    private static final LocalDate TERM_END = LocalDate.of(2026, 12, 31);
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);

    @Test
    void cancellingPolicyChangesStatusAndRecordsTheEffectiveDate() {
        Policy policy = termPolicy();

        policy.cancel(TODAY, TODAY);

        assertThat(policy.getStatus()).isEqualTo(PolicyStatus.CANCELLED);
        assertThat(policy.getCancelledOn()).isEqualTo(TODAY);
        assertThat(policy.getCoverageEndsOn()).isEqualTo(TODAY);
    }

    @Test
    void cancelledPolicyStillCoversLossesUpToTheCancellationDate() {
        Policy policy = termPolicy();
        policy.cancel(TODAY, TODAY);

        // Loss-occurrence principle: losses before (and on) the cancellation date stay covered.
        assertThat(policy.covers(TERM_START)).isTrue();
        assertThat(policy.covers(TODAY.minusDays(10))).isTrue();
        assertThat(policy.covers(TODAY)).isTrue();
        // Losses after the cancellation are not covered any more.
        assertThat(policy.covers(TODAY.plusDays(1))).isFalse();
        assertThat(policy.covers(TERM_END)).isFalse();
    }

    @Test
    void cancellationCanTakeEffectInTheFuture() {
        Policy policy = termPolicy();
        LocalDate endOfMonth = LocalDate.of(2026, 6, 30);

        policy.cancel(endOfMonth, TODAY);

        assertThat(policy.covers(endOfMonth)).isTrue();
        assertThat(policy.covers(endOfMonth.plusDays(1))).isFalse();
    }

    @Test
    void cancellationCannotRemoveCoverageRetroactively() {
        Policy policy = termPolicy();

        assertThatThrownBy(() -> policy.cancel(TODAY.minusDays(1), TODAY))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("retroactively");
        assertThat(policy.getStatus()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(policy.getCancelledOn()).isNull();
    }

    @Test
    void cancellationDateMustNotBeAfterTheEndOfTheTerm() {
        Policy policy = termPolicy();

        assertThatThrownBy(() -> policy.cancel(TERM_END.plusDays(1), TODAY))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("end of the policy term");
    }

    @Test
    void cancellingBeforeTheTermStartsMeansNoCoverageAtAll() {
        Policy futurePolicy = new Policy(1L, 2L, LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31),
                BigDecimal.TEN, BigDecimal.ONE);

        futurePolicy.cancel(TODAY, TODAY);

        assertThat(futurePolicy.covers(LocalDate.of(2027, 1, 1))).isFalse();
        assertThat(futurePolicy.covers(TODAY)).isFalse();
    }

    @Test
    void activePolicyCoversItsWholeTermInclusive() {
        Policy policy = termPolicy();

        assertThat(policy.covers(TERM_START.minusDays(1))).isFalse();
        assertThat(policy.covers(TERM_START)).isTrue();
        assertThat(policy.covers(TERM_END)).isTrue();
        assertThat(policy.covers(TERM_END.plusDays(1))).isFalse();
    }

    @Test
    void cancelledPolicyCannotBeCancelledAgain() {
        Policy policy = termPolicy();
        policy.cancel(TODAY, TODAY);

        assertThatThrownBy(() -> policy.cancel(TODAY, TODAY))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Only an active policy");
    }

    private static Policy termPolicy() {
        return new Policy(1L, 2L, TERM_START, TERM_END, BigDecimal.TEN, BigDecimal.ONE);
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
}
