package de.hunar.claim_tracker.repository;

import de.hunar.claim_tracker.entity.Claim;
import de.hunar.claim_tracker.entity.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {


    List<Claim> findByStatus(ClaimStatus status);
    List<Claim> findByCustomerNumber(String customerNumber);
    List<Claim> findByStatusOrderByCreatedAtDesc(ClaimStatus status);
}
