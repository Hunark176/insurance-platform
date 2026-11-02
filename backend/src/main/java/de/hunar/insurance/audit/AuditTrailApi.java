package de.hunar.insurance.audit;

public interface AuditTrailApi {
    AuditContext captureContext();

    void record(String action, String entityType, String entityId, String summary, AuditContext context);
}
