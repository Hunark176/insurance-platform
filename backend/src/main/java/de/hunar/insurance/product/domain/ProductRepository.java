package de.hunar.insurance.product.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    java.util.Optional<Product> findByCode(String code);
}
