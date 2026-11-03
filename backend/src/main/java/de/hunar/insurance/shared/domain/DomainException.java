package de.hunar.insurance.shared.domain;

/**
 * Signals a rejected operation caused by a business rule rather than an infrastructure failure.
 */
public class DomainException extends RuntimeException {
    /**
     * Creates a business-rule exception with a diagnostic message.
     *
     * @param message explanation of the violated rule
     */
    public DomainException(String message) {
        super(message);
    }
}
