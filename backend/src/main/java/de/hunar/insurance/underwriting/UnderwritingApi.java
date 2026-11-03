package de.hunar.insurance.underwriting;

/**
 * Public underwriting-module contract for policy-issuance eligibility checks.
 */
public interface UnderwritingApi {
    /**
     * Determines whether the customer and product can be used to issue a policy.
     *
     * @param customerId customer identifier to evaluate
     * @param productId product identifier to evaluate
     * @return {@code true} when the customer exists and the product is active
     */
    boolean canIssue(Long customerId, Long productId);
}
