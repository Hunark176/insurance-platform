package de.hunar.insurance.claim.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Page<Claim> findByPolicyId(Long policyId, Pageable pageable);
    List<Claim> findByStatus(ClaimStatus status);
    List<Claim> findByStatusOrderByCreatedAtDesc(ClaimStatus status);

    /**
     * Sums the amounts of approved claims of a policy whose loss date lies in the given period.
     * Used for the annual aggregate limit; the loss date decides which policy year a claim
     * belongs to, not the date of approval.
     *
     * @param policyId policy identifier
     * @param from first day of the period (inclusive)
     * @param to last day of the period (inclusive)
     * @return total approved amount, zero if there is none
     */
    @Query("""
            select coalesce(sum(c.amount), 0) from Claim c
            where c.policyId = :policyId
              and c.status = de.hunar.insurance.claim.domain.ClaimStatus.APPROVED
              and c.occurredOn between :from and :to
            """)
    BigDecimal sumApprovedAmount(@Param("policyId") Long policyId,
                                 @Param("from") LocalDate from,
                                 @Param("to") LocalDate to);
}
