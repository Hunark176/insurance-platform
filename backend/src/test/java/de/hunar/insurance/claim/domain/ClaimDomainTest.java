package de.hunar.insurance.claim.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimDomainTest {

    @Test
    void newClaimStartsInReceivedState() {
        Claim claim = newClaim();

        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.RECEIVED);
        assertThat(claim.getCreatedAt()).isNull();
        assertThat(claim.getUpdatedAt()).isNull();
    }

    @Test
    void claimCanOnlyFollowAllowedStatusTransitions() {
        Claim claim = newClaim();

        claim.transitionTo(ClaimStatus.IN_REVIEW);
        claim.transitionTo(ClaimStatus.APPROVED);

        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.APPROVED);
        assertThatThrownBy(() -> claim.transitionTo(ClaimStatus.REJECTED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ungültiger Status-Übergang");
    }

    @Test
    void claimCannotSkipReviewOrUseNullStatus() {
        Claim claim = newClaim();

        assertThatThrownBy(() -> claim.transitionTo(ClaimStatus.APPROVED))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> claim.transitionTo(null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.RECEIVED);
    }

    @Test
    void claimRejectsInvalidConstructionValues() {
        assertThatThrownBy(() -> Claim.create(0L, LocalDate.now(), ClaimType.AUTO, null, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("policyId");
        assertThatThrownBy(() -> Claim.create(1L, LocalDate.now(), ClaimType.AUTO, null, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
        assertThatThrownBy(() -> Claim.create(1L, LocalDate.now(), ClaimType.AUTO, "x".repeat(1001),
                BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("description");
    }

    private Claim newClaim() {
        return Claim.create(1L, LocalDate.of(2026, 9, 20), ClaimType.AUTO, "Damage", new BigDecimal("10.00"));
    }
}
