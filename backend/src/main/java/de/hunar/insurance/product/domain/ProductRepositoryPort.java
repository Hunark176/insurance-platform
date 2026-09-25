package de.hunar.insurance.product.domain;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
    Product save(Product product);
    List<Product> findAll();
    Optional<Product> findById(Long id);
    boolean existsById(Long id);
}
