package de.hunar.insurance.policy.mapper;

import de.hunar.insurance.customer.CustomerSummary;
import de.hunar.insurance.product.ProductSummary;
import de.hunar.insurance.policy.PolicySnapshot;
import de.hunar.insurance.policy.domain.Policy;
import de.hunar.insurance.policy.dto.PolicyResponse;
import de.hunar.insurance.shared.domain.Money;
import org.springframework.stereotype.Component;

@Component
public class PolicyMapper {
    public PolicyResponse toResponse(Policy p, CustomerSummary customer, ProductSummary product) {
        return new PolicyResponse(p.getId(), p.getCustomerId(), customer.customerNumber(), customer.displayName(),
                p.getProductId(), product.name(), p.getValidFrom(), p.getValidTo(), p.getCoverageLimit(),
                p.getPremium(), p.getStatus());
    }
    public PolicySnapshot toSnapshot(Policy p, CustomerSummary customer) {
        return new PolicySnapshot(p.getId(), p.getCustomerId(), customer.customerNumber(), customer.displayName(),
                p.getProductId(),
                p.getValidFrom(), p.getValidTo(), Money.eur(p.getCoverageLimit()));
    }
}
