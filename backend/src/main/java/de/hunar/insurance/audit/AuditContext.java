package de.hunar.insurance.audit;

public record AuditContext(String actor, String requestId) {
}
