package de.hunar.insurance.customer.controller;

import de.hunar.insurance.customer.application.CustomerService;
import de.hunar.insurance.customer.dto.CreateCustomerRequest;
import de.hunar.insurance.customer.dto.CustomerResponse;
import de.hunar.insurance.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * Exposes public customer creation and lookup operations.
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService service;

    /**
     * Lists customers page by page for clerks and administrators, ordered by identifier.
     *
     * @param page zero-based page index
     * @param size page size from 1 to 100
     * @return the requested page of customers
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @SecurityRequirement(name = "basicAuth")
    public PageResponse<CustomerResponse> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return service.findPage(PageRequest.of(page, size, Sort.by("id")));
    }

    /**
     * Creates a customer from validated input.
     *
     * @param request customer details
     * @return the created customer
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @SecurityRequirement(name = "basicAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CreateCustomerRequest request) {
        return service.create(request);
    }

    /**
     * Returns a customer lookup response without exposing the customer's email address.
     *
     * @param id customer identifier
     * @return customer identity and a null email field
     */
    @GetMapping("/{id}")
    public CustomerResponse findById(@PathVariable Long id) {
        var summary = service.findById(id);
        return new CustomerResponse(summary.id(), summary.customerNumber(), summary.displayName(), null, true);
    }
}
