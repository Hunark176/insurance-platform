package de.hunar.insurance.policy.domain;

import java.util.Optional;

public interface PolicyRepositoryPort {
    Policy save(Policy policy);
    Optional<Policy> findById(Long id);
}
