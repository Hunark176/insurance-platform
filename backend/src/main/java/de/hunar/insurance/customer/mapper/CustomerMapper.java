package de.hunar.insurance.customer.mapper;

import de.hunar.insurance.customer.CustomerSummary;
import de.hunar.insurance.customer.domain.Customer;
import de.hunar.insurance.customer.dto.CustomerResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getCustomerNumber(), customer.getDisplayName(),
                customer.getEmail(), customer.isActive());
    }

    public CustomerSummary toSummary(Customer customer) {
        return new CustomerSummary(customer.getId(), customer.getCustomerNumber(), customer.getDisplayName());
    }
}
