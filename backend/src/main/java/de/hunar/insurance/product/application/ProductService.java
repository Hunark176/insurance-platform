package de.hunar.insurance.product.application;

import de.hunar.insurance.audit.AuditTrailApi;
import de.hunar.insurance.product.ProductApi;
import de.hunar.insurance.product.ProductSummary;
import de.hunar.insurance.product.domain.Product;
import de.hunar.insurance.product.domain.ProductRepository;
import de.hunar.insurance.product.dto.CreateProductRequest;
import de.hunar.insurance.product.dto.ProductResponse;
import de.hunar.insurance.product.mapper.ProductMapper;
import de.hunar.insurance.shared.web.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Implements product catalog use cases and supplies product summaries to other modules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService implements ProductApi {
    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final AuditTrailApi auditTrail;

    /**
     * Creates a product, records the creation, and returns its API representation.
     *
     * @param request validated product details and terms
     * @return the newly created product
     */
    @Transactional
    public ProductResponse create(CreateProductRequest r) {
        Product product = repository.save(new Product(r.code(), r.name(), r.coverageLimit(), r.premium(),
                r.annualAggregateFactor()));
        auditTrail.record("CREATED", "PRODUCT", product.getId().toString(),
                "product created", auditTrail.captureContext());
        log.info("Product created: productId={}", product.getId());
        return mapper.toResponse(product);
    }
    /**
     * Lists all products.
     *
     * @return all product responses
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() { return repository.findAll().stream().map(mapper::toResponse).toList(); }
    /**
     * Retrieves product details for the product-module API.
     *
     * @param id product identifier
     * @return product summary
     * @throws ResourceNotFoundException if the product does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public ProductSummary findById(Long id) {
        return mapper.toSummary(repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", id)));
    }

    /**
     * Resolves several products with one {@code IN} query.
     *
     * @param ids product identifiers; duplicates are ignored
     * @return summaries for the products found
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductSummary> findByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return repository.findAllById(new HashSet<>(ids)).stream().map(mapper::toSummary).toList();
    }
    /**
     * Checks whether a product exists.
     *
     * @param id product identifier
     * @return whether the product exists
     */
    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long id) { return repository.existsById(id); }
}
