package de.hunar.insurance.product.controller;

import de.hunar.insurance.product.application.ProductService;
import de.hunar.insurance.product.dto.CreateProductRequest;
import de.hunar.insurance.product.dto.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;
    @GetMapping @PreAuthorize("isAuthenticated()")
    public List<ProductResponse> findAll() { return service.findAll(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('CLERK','ADMIN')")
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) { return service.create(request); }
    @GetMapping("/{id}") @PreAuthorize("isAuthenticated()")
    public ProductResponse findById(@PathVariable Long id) {
        var p = service.findById(id);
        return new ProductResponse(p.id(), p.code(), p.name(), p.coverageLimit(), p.premium(), p.active());
    }
}
