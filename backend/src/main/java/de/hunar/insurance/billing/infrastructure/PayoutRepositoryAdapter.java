package de.hunar.insurance.billing.infrastructure;

import de.hunar.insurance.billing.domain.Payout;
import de.hunar.insurance.billing.domain.PayoutRepository;
import de.hunar.insurance.billing.domain.PayoutRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PayoutRepositoryAdapter implements PayoutRepositoryPort {
    private final PayoutRepository jpaRepository;

    public boolean existsByClaimId(Long claimId) { return jpaRepository.existsByClaimId(claimId); }
    public Payout save(Payout payout) { return ((CrudRepository<Payout, Long>) jpaRepository).save(payout); }
    public List<Payout> findAll() { return jpaRepository.findAll(); }
    public Optional<Payout> findById(Long id) { return ((CrudRepository<Payout, Long>) jpaRepository).findById(id); }
}
