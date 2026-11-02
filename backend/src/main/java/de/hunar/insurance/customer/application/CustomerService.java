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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService implements CustomerApi {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;
    private final AuditTrailApi auditTrail;

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        Customer customer = repository.save(
                new Customer(request.customerNumber(), request.displayName(), request.email()));
        auditTrail.record("CREATED", "CUSTOMER", customer.getId().toString(),
                "customer created", auditTrail.captureContext());
        log.info("Customer created: customerId={}", customer.getId());
        return mapper.toResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerSummary findById(Long id) {
        return mapper.toSummary(repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerSummary findByNumber(String number) {
        return mapper.toSummary(repository.findByCustomerNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", number)));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return repository.existsById(id);
    }
}
