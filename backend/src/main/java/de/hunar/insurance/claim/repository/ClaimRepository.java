package de.hunar.insurance.claim.repository;

import de.hunar.insurance.claim.entity.Claim;
import de.hunar.insurance.claim.entity.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Page<Claim> findByPolicyId(Long policyId, Pageable pageable);
    List<Claim> findByStatus(ClaimStatus status);
    List<Claim> findByStatusOrderByCreatedAtDesc(ClaimStatus status);
}
