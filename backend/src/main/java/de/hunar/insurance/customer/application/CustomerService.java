package de.hunar.insurance.customer.application;

import de.hunar.insurance.audit.AuditTrailApi;
import de.hunar.insurance.customer.CustomerApi;
import de.hunar.insurance.customer.CustomerSummary;
import de.hunar.insurance.customer.domain.Customer;
import de.hunar.insurance.customer.domain.CustomerRepository;
import de.hunar.insurance.customer.dto.CreateCustomerRequest;
import de.hunar.insurance.customer.dto.CustomerResponse;
import de.hunar.insurance.customer.mapper.CustomerMapper;
import de.hunar.insurance.shared.web.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Implements customer use cases and exposes customer summaries to collaborating modules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService implements CustomerApi {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;
    private final AuditTrailApi auditTrail;

    /**
     * Creates a customer, records the business action, and returns its API representation.
     *
     * @param request validated customer details
     * @return the newly created customer
     */
    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        Customer customer = repository.save(
                new Customer(request.customerNumber(), request.displayName(), request.email()));
        auditTrail.record("CREATED", "CUSTOMER", customer.getId().toString(),
                "customer created", auditTrail.captureContext());
        log.info("Customer created: customerId={}", customer.getId());
        return mapper.toResponse(customer);
    }

    /**
     * Lists all customers for authorized staff workflows.
     *
     * @return all customer responses
     */
    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    /**
     * Resolves the customer-module contract by persistent identifier.
     *
     * @param id customer identifier
     * @return minimal customer summary
     * @throws ResourceNotFoundException if no customer has the given identifier
     */
    @Override
    @Transactional(readOnly = true)
    public CustomerSummary findById(Long id) {
        return mapper.toSummary(repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", id)));
    }

    /**
     * Resolves several customers with one {@code IN} query.
     *
     * @param ids customer identifiers; duplicates are ignored
     * @return summaries for the customers found
     */
    @Override
    @Transactional(readOnly = true)
    public List<CustomerSummary> findByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return repository.findAllById(new HashSet<>(ids)).stream().map(mapper::toSummary).toList();
    }

    /**
     * Resolves the customer-module contract by customer number.
     *
     * @param number customer number
     * @return minimal customer summary
     * @throws ResourceNotFoundException if no customer has the given number
     */
    @Override
    @Transactional(readOnly = true)
    public CustomerSummary findByNumber(String number) {
        return mapper.toSummary(repository.findByCustomerNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", number)));
    }

    /**
     * Checks customer existence without loading the complete customer record.
     *
     * @param id customer identifier
     * @return whether the customer exists
     */
    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return repository.existsById(id);
    }
}
