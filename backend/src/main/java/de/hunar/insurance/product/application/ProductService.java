package de.hunar.insurance.product.application;

import de.hunar.insurance.product.ProductApi;
import de.hunar.insurance.product.ProductSummary;
import de.hunar.insurance.product.domain.Product;
import de.hunar.insurance.product.domain.ProductRepository;
import de.hunar.insurance.product.dto.CreateProductRequest;
import de.hunar.insurance.product.dto.ProductResponse;
import de.hunar.insurance.product.mapper.ProductMapper;
import de.hunar.insurance.shared.web.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService implements ProductApi {
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Transactional
    public ProductResponse create(CreateProductRequest r) {
        return mapper.toResponse(repository.save(new Product(r.code(), r.name(), r.coverageLimit(), r.premium())));
    }
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() { return repository.findAll().stream().map(mapper::toResponse).toList(); }
    @Override @Transactional(readOnly = true)
    public ProductSummary findById(Long id) {
        return mapper.toSummary(repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", id)));
    }
    @Override @Transactional(readOnly = true)
    public boolean exists(Long id) { return repository.existsById(id); }
}
