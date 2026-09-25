package de.hunar.insurance.billing.domain;

import java.util.List;
import java.util.Optional;

public interface PayoutRepositoryPort {
    boolean existsByClaimId(Long claimId);
    Payout save(Payout payout);
    List<Payout> findAll();
    Optional<Payout> findById(Long id);
}
