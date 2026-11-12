package de.hunar.insurance.policy.domain;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findFirstByCustomerIdAndProductId(Long customerId, Long productId);

    /**
     * Loads a policy and locks its row ({@code SELECT ... FOR UPDATE}) until the current
     * transaction ends, so concurrent claim approvals for the same policy run one after another.
     *
     * @param id policy identifier
     * @return the locked policy, if it exists
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Policy p where p.id = :id")
    Optional<Policy> findByIdForUpdate(@Param("id") Long id);
}
