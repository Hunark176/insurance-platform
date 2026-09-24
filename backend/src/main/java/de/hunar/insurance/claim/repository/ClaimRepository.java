package de.hunar.insurance.claim.repository;

import de.hunar.insurance.claim.entity.Claim;
import de.hunar.insurance.claim.entity.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {


    List<Claim> findByStatus(ClaimStatus status);
    List<Claim> findByCustomerNumber(String customerNumber);
    List<Claim> findByStatusOrderByCreatedAtDesc(ClaimStatus status);
}
