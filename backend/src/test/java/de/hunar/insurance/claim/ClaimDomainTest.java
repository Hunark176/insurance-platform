package de.hunar.insurance.claim.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ClaimDomainTest {
    @Test
    void claimDefaultsToReceivedWhenPersisted() {
        Claim claim = new Claim();
        claim.setPolicyId(1L);
        claim.setOccurredOn(LocalDate.now());
        claim.setClaimType(ClaimType.AUTO);
        claim.setAmount(new BigDecimal("10.00"));

        claim.onCreate();

        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.RECEIVED);
        assertThat(claim.getCreatedAt()).isNotNull();
    }
}
