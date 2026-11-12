package de.hunar.insurance.customer;

import java.util.Collection;
import java.util.List;

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
     * Loads several customers with a single query; use this instead of calling
     * {@link #findById(Long)} in a loop.
     *
     * @param customerIds customer identifiers; duplicates are allowed
     * @return summaries for the customers found; unknown identifiers are omitted
     */
    List<CustomerSummary> findByIds(Collection<Long> customerIds);

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
