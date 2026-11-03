package de.hunar.insurance.customer;

/**
 * Minimal customer data shared across module boundaries.
 *
 * @param id persistent customer identifier
 * @param customerNumber externally visible customer number
 * @param displayName customer name suitable for display
 */
public record CustomerSummary(Long id, String customerNumber, String displayName) {
}
