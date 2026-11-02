package de.hunar.insurance.audit.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {
    Page<AuditEntry> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
            String entityType,
            String entityId,
            Pageable pageable
    );
}
