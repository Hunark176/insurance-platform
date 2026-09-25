package de.hunar.insurance.policy.infrastructure;

import de.hunar.insurance.policy.domain.Policy;
import de.hunar.insurance.policy.domain.PolicyRepository;
import de.hunar.insurance.policy.domain.PolicyRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PolicyRepositoryAdapter implements PolicyRepositoryPort {
    private final PolicyRepository jpaRepository;
    public PolicyRepositoryAdapter(PolicyRepository jpaRepository) { this.jpaRepository = jpaRepository; }
    public Policy save(Policy policy) { return jpaRepository.save(policy); }
    public Optional<Policy> findById(Long id) { return jpaRepository.findById(id); }
}
