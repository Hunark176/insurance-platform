package de.hunar.insurance.product.infrastructure;

import de.hunar.insurance.product.domain.Product;
import de.hunar.insurance.product.domain.ProductRepository;
import de.hunar.insurance.product.domain.ProductRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryAdapter implements ProductRepositoryPort {
    private final ProductRepository jpaRepository;
    public ProductRepositoryAdapter(ProductRepository jpaRepository) { this.jpaRepository = jpaRepository; }
    public Product save(Product product) { return jpaRepository.save(product); }
    public List<Product> findAll() { return jpaRepository.findAll(); }
    public Optional<Product> findById(Long id) { return jpaRepository.findById(id); }
    public boolean existsById(Long id) { return jpaRepository.existsById(id); }
}
