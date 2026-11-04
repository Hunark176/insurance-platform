package de.hunar.insurance.product.controller;

import de.hunar.insurance.product.application.ProductService;
import de.hunar.insurance.product.dto.CreateProductRequest;
import de.hunar.insurance.product.dto.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes public product catalog reads and creation.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;
    /**
     * Lists all products in the catalog.
     *
     * @return all product responses
     */
    @GetMapping
    public List<ProductResponse> findAll() { return service.findAll(); }
    /**
     * Creates a product from validated product details.
     *
     * @param request product details
     * @return the created product
     */
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) { return service.create(request); }
    /**
     * Retrieves a product by identifier.
     *
     * @param id product identifier
     * @return the product details
     */
    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id) {
        var p = service.findById(id);
        return new ProductResponse(p.id(), p.code(), p.name(), p.coverageLimit(), p.premium(), p.active());
    }
}
