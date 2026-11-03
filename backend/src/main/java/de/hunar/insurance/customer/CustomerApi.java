package de.hunar.insurance.customer;

/**
 * Public customer-module contract for other application modules.
 *
 * <p>Consumers receive customer summaries rather than depending on customer entities or repositories.</p>
 */
public interface CustomerApi {
    /**
     * Finds a customer by its persistent identifier.
     *
     * @param customerId customer identifier
     * @return customer details needed by other modules
     * @throws de.hunar.insurance.shared.web.ResourceNotFoundException if the customer does not exist
     */
    CustomerSummary findById(Long customerId);

    /**
     * Finds a customer by the externally visible customer number.
     *
     * @param customerNumber customer number
     * @return customer details needed by other modules
     * @throws de.hunar.insurance.shared.web.ResourceNotFoundException if the customer does not exist
     */
    CustomerSummary findByNumber(String customerNumber);

    /**
     * Checks whether a customer identifier exists.
     *
     * @param customerId customer identifier
     * @return {@code true} when a customer with this identifier exists
     */
    boolean exists(Long customerId);
}
