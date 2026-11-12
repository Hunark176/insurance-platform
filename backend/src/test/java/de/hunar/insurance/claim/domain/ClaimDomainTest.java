package de.hunar.insurance.claim.domain;

import de.hunar.insurance.shared.domain.DomainException;
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
        claim.approve(new BigDecimal("1000.00"), true);

        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.APPROVED);
        assertThatThrownBy(() -> claim.transitionTo(ClaimStatus.REJECTED))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Ungültiger Status-Übergang");
    }

    @Test
    void claimCannotSkipReviewOrUseNullStatus() {
        Claim claim = newClaim();

        assertThatThrownBy(() -> claim.approve(new BigDecimal("1000.00"), true))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Ungültiger Status-Übergang");
        assertThatThrownBy(() -> claim.transitionTo(null))
                .isInstanceOf(DomainException.class);
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.RECEIVED);
    }

    @Test
    void approvalCannotBypassTheCoverageRules() {
        Claim claim = inReview();

        assertThatThrownBy(() -> claim.transitionTo(ClaimStatus.APPROVED))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("approve()");
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.IN_REVIEW);
    }

    @Test
    void approvalRequiresCoverageOnTheLossDate() {
        Claim claim = inReview();

        assertThatThrownBy(() -> claim.approve(new BigDecimal("1000.00"), false))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("coverage on the date of loss");
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.IN_REVIEW);
    }

    @Test
    void approvalRequiresAnAmountWithinTheCoverageLimit() {
        Claim claim = inReview();
        BigDecimal limitBelowAmount = claim.getAmount().subtract(new BigDecimal("0.01"));

        assertThatThrownBy(() -> claim.approve(limitBelowAmount, true))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("coverage limit");
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.IN_REVIEW);

        claim.approve(claim.getAmount(), true);
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.APPROVED);
    }

    private Claim inReview() {
        Claim claim = newClaim();
        claim.transitionTo(ClaimStatus.IN_REVIEW);
        return claim;
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
