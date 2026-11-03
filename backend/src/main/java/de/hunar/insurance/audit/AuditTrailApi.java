package de.hunar.insurance.audit;

/**
 * Module boundary for recording and propagating audit metadata.
 *
 * <p>Writes must participate in the business transaction so an audit entry cannot outlive
 * a rolled-back business change.</p>
 */
public interface AuditTrailApi {
    /**
     * Captures the current authenticated actor and request correlation identifier.
     *
     * @return context to attach to an audit record or propagate with a domain event
     */
    AuditContext captureContext();

    /**
     * Appends an audit record to the current transaction.
     *
     * @param action business action such as {@code CREATED} or {@code STATUS_CHANGED}
     * @param entityType type of affected business entity
     * @param entityId identifier of the affected entity
     * @param summary data-minimized description of the change
     * @param context actor and request context captured at the initiating boundary
     */
    void record(String action, String entityType, String entityId, String summary, AuditContext context);
}
