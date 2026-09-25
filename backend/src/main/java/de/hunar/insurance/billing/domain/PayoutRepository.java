package de.hunar.insurance.billing.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutRepository extends JpaRepository<Payout, Long> {
    boolean existsByClaimId(Long claimId);
}
