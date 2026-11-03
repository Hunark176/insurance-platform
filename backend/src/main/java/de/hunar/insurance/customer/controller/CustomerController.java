package de.hunar.insurance.customer.controller;

import de.hunar.insurance.customer.application.CustomerService;
import de.hunar.insurance.customer.dto.CreateCustomerRequest;
import de.hunar.insurance.customer.dto.CustomerResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes customer creation and authorized customer lookup operations.
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService service;

    /**
     * Lists customers for clerks and administrators.
     *
     * @return all customer responses
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CLERK','ADMIN')")
    public List<CustomerResponse> findAll() {
        return service.findAll();
    }

    /**
     * Creates a customer from validated input.
     *
     * @param request customer details
     * @return the created customer
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CLERK','ADMIN')")
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
    @PreAuthorize("hasAnyRole('CUSTOMER','CLERK','ADMIN')")
    public CustomerResponse findById(@PathVariable Long id) {
        var summary = service.findById(id);
        return new CustomerResponse(summary.id(), summary.customerNumber(), summary.displayName(), null, true);
    }
}
