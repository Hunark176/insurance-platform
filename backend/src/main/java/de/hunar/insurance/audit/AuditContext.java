package de.hunar.insurance.audit;

/**
 * Correlation metadata carried with a business change, including across module events.
 *
 * @param actor authenticated user name, or {@code SYSTEM} for non-user initiated work
 * @param requestId request correlation identifier; may be {@code null} for background work
 */
public record AuditContext(String actor, String requestId) {
}
