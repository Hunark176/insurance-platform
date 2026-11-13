package de.hunar.insurance.billing.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditNoteRepository extends JpaRepository<CreditNote, Long> {
    boolean existsByPolicyId(Long policyId);

    Optional<CreditNote> findByPolicyId(Long policyId);
}
