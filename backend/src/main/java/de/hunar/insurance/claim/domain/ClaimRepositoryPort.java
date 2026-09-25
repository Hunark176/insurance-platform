package de.hunar.insurance.claim.domain;

import de.hunar.insurance.claim.entity.Claim;

import java.util.List;
import java.util.Optional;

public interface ClaimRepositoryPort {
    List<Claim> findAll();
    Optional<Claim> findById(Long id);
    Claim save(Claim claim);
    boolean existsById(Long id);
    void deleteById(Long id);
}
