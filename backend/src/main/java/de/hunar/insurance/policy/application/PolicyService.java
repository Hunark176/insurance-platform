package de.hunar.insurance.policy.application;

import de.hunar.insurance.customer.CustomerApi;
import de.hunar.insurance.policy.PolicyApi;
import de.hunar.insurance.policy.PolicySnapshot;
import de.hunar.insurance.policy.domain.Policy;
import de.hunar.insurance.policy.domain.PolicyRepository;
import de.hunar.insurance.policy.domain.PolicyStatus;
import de.hunar.insurance.policy.dto.IssuePolicyRequest;
import de.hunar.insurance.policy.dto.PolicyResponse;
import de.hunar.insurance.policy.events.PolicyIssuedEvent;
import de.hunar.insurance.policy.mapper.PolicyMapper;
import de.hunar.insurance.product.ProductApi;
import de.hunar.insurance.shared.domain.DomainException;
import de.hunar.insurance.shared.domain.Money;
import de.hunar.insurance.shared.web.ResourceNotFoundException;
import de.hunar.insurance.underwriting.UnderwritingApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PolicyService implements PolicyApi {
    private final PolicyRepository repository;
    private final CustomerApi customerApi;
    private final ProductApi productApi;
    private final UnderwritingApi underwritingApi;
    private final PolicyMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PolicyResponse issue(IssuePolicyRequest request) {
        var product = productApi.findById(request.productId());
        if (!customerApi.exists(request.customerId())) {
            throw new ResourceNotFoundException("Customer", request.customerId());
        }
        if (!underwritingApi.canIssue(request.customerId(), request.productId())) {
            throw new DomainException("Policy cannot be issued for the supplied customer and product");
        }
        if (!request.validTo().isAfter(request.validFrom())) {
            throw new DomainException("validTo must be after validFrom");
        }
        var policy = repository.save(new Policy(request.customerId(), request.productId(), request.validFrom(),
                request.validTo(), product.coverageLimit(), product.premium()));
        eventPublisher.publishEvent(new PolicyIssuedEvent(policy.getId(), policy.getCustomerId(), Money.eur(policy.getPremium())));
        return mapper.toResponse(policy);
    }

    @Transactional(readOnly = true)
    public PolicyResponse findByIdResponse(Long id) {
        return mapper.toResponse(get(id));
    }

    @Transactional
    public PolicyResponse cancel(Long id) {
        Policy policy = get(id);
        policy.cancel();
        return mapper.toResponse(repository.save(policy));
    }

    @Override
    @Transactional(readOnly = true)
    public PolicySnapshot getSnapshot(Long id) {
        return mapper.toSnapshot(get(id));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActiveAt(Long id, LocalDate date) {
        Policy p = get(id);
        return p.getStatus() == PolicyStatus.ACTIVE && !date.isBefore(p.getValidFrom()) && !date.isAfter(p.getValidTo());
    }

    private Policy get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Policy", id));
    }
}
