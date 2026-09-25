package de.hunar.insurance.billing.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    boolean existsByPolicyId(Long policyId);
}
