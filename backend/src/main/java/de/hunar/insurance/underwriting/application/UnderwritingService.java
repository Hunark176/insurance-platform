package de.hunar.insurance.underwriting.application;

import de.hunar.insurance.customer.CustomerApi;
import de.hunar.insurance.product.ProductApi;
import de.hunar.insurance.underwriting.UnderwritingApi;
import de.hunar.insurance.underwriting.domain.UnderwritingDecision;
import de.hunar.insurance.underwriting.dto.UnderwritingCheckRequest;
import de.hunar.insurance.underwriting.dto.UnderwritingCheckResponse;
import de.hunar.insurance.underwriting.mapper.UnderwritingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnderwritingService implements UnderwritingApi {
    private final CustomerApi customerApi;
    private final ProductApi productApi;
    private final UnderwritingMapper mapper;

    @Override
    public boolean canIssue(Long customerId, Long productId) {
        boolean customerExists = customerApi.exists(customerId);
        boolean productActive = customerExists && productApi.findById(productId).active();
        boolean eligible = customerExists && productActive;
        log.debug("Policy issuance eligibility evaluated: customerId={} productId={} customerExists={} productActive={}",
                customerId, productId, customerExists, productActive);
        return eligible;
    }

    public UnderwritingCheckResponse check(UnderwritingCheckRequest request) {
        boolean eligible = canIssue(request.customerId(), request.productId());
        log.info("Underwriting check completed: customerId={} productId={} eligible={}",
                request.customerId(), request.productId(), eligible);
        return mapper.toResponse(new UnderwritingDecision(request.customerId(), request.productId(), eligible,
                eligible ? "Eligible for issue" : "Customer or product is not eligible"));
    }
}
