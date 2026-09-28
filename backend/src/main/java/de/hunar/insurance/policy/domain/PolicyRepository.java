package de.hunar.insurance.policy.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    java.util.Optional<Policy> findFirstByCustomerIdAndProductId(Long customerId, Long productId);
}
