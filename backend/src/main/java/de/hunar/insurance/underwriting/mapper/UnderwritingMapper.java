package de.hunar.insurance.underwriting.mapper;

import de.hunar.insurance.underwriting.domain.UnderwritingDecision;
import de.hunar.insurance.underwriting.dto.UnderwritingCheckResponse;
import org.springframework.stereotype.Component;

@Component
public class UnderwritingMapper {
    public UnderwritingCheckResponse toResponse(UnderwritingDecision decision) {
        return new UnderwritingCheckResponse(decision.customerId(), decision.productId(), decision.eligible(),
                decision.reason());
    }
}
