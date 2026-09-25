package de.hunar.insurance.product;

public interface ProductApi {
    boolean exists(Long productId);
    ProductSummary findById(Long productId);
}
