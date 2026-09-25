package de.hunar.insurance.underwriting.application;

import de.hunar.insurance.customer.CustomerApi;
import de.hunar.insurance.product.ProductApi;
import de.hunar.insurance.underwriting.UnderwritingApi;
import de.hunar.insurance.underwriting.domain.UnderwritingDecision;
import de.hunar.insurance.underwriting.dto.UnderwritingCheckRequest;
import de.hunar.insurance.underwriting.dto.UnderwritingCheckResponse;
import de.hunar.insurance.underwriting.mapper.UnderwritingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnderwritingService implements UnderwritingApi {
    private final CustomerApi customerApi;
    private final ProductApi productApi;
    private final UnderwritingMapper mapper;

    @Override
    public boolean canIssue(Long customerId, Long productId) {
        return customerApi.exists(customerId) && productApi.findById(productId).active();
    }

    public UnderwritingCheckResponse check(UnderwritingCheckRequest request) {
        boolean eligible = canIssue(request.customerId(), request.productId());
        return mapper.toResponse(new UnderwritingDecision(request.customerId(), request.productId(), eligible,
                eligible ? "Eligible for issue" : "Customer or product is not eligible"));
    }
}
