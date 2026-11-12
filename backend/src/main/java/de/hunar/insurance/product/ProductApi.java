package de.hunar.insurance.product;

import java.util.Collection;
import java.util.List;

/**
 * Public product-module contract used by other modules without exposing persistence details.
 */
public interface ProductApi {
    /**
     * Checks whether a product identifier exists.
     *
     * @param productId product identifier
     * @return {@code true} when the product exists
     */
    boolean exists(Long productId);

    /**
     * Retrieves product terms and availability for another module.
     *
     * @param productId product identifier
     * @return product summary including active state
     * @throws de.hunar.insurance.shared.web.ResourceNotFoundException if the product does not exist
     */
    ProductSummary findById(Long productId);

    /**
     * Loads several products with a single query; use this instead of calling
     * {@link #findById(Long)} in a loop.
     *
     * @param productIds product identifiers; duplicates are allowed
     * @return summaries for the products found; unknown identifiers are omitted
     */
    List<ProductSummary> findByIds(Collection<Long> productIds);
}
