package de.hunar.insurance.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "audit_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(nullable = false, length = 100, updatable = false)
    private String actor;

    @Column(nullable = false, length = 40, updatable = false)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 40, updatable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false, length = 100, updatable = false)
    private String entityId;

    @Column(name = "request_id", length = 36, updatable = false)
    private String requestId;

    @Column(length = 500, updatable = false)
    private String summary;

    public AuditEntry(
            Instant occurredAt,
            String actor,
            String action,
            String entityType,
            String entityId,
            String requestId,
            String summary
    ) {
        this.occurredAt = occurredAt;
        this.actor = actor;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.requestId = requestId;
        this.summary = summary;
    }

    @PreUpdate
    @PreRemove
    private void preventMutation() {
        throw new UnsupportedOperationException("Audit entries are append-only");
    }
}
