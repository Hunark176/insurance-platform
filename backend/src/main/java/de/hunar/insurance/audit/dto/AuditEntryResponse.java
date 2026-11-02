package de.hunar.insurance.audit.dto;

import java.time.Instant;

public record AuditEntryResponse(
        Long id,
        Instant occurredAt,
        String actor,
        String action,
        String entityType,
        String entityId,
        String requestId,
        String summary
) {
}
