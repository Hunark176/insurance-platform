package de.hunar.insurance.policy.mapper;

import de.hunar.insurance.policy.PolicySnapshot;
import de.hunar.insurance.policy.domain.Policy;
import de.hunar.insurance.policy.dto.PolicyResponse;
import de.hunar.insurance.shared.domain.Money;
import org.springframework.stereotype.Component;

@Component
public class PolicyMapper {
    public PolicyResponse toResponse(Policy p) {
        return new PolicyResponse(p.getId(), p.getCustomerId(), p.getProductId(), p.getValidFrom(), p.getValidTo(),
                p.getCoverageLimit(), p.getPremium(), p.getStatus());
    }
    public PolicySnapshot toSnapshot(Policy p) {
        return new PolicySnapshot(p.getId(), p.getCustomerId(), p.getProductId(), p.getValidFrom(), p.getValidTo(),
                Money.eur(p.getCoverageLimit()));
    }
}
