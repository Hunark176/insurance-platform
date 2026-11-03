package de.hunar.insurance.policy.application;

import de.hunar.insurance.audit.AuditTrailApi;
import de.hunar.insurance.audit.AuditContext;
import de.hunar.insurance.customer.CustomerApi;
import de.hunar.insurance.customer.CustomerSummary;
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
import de.hunar.insurance.product.ProductSummary;
import de.hunar.insurance.shared.domain.DomainException;
import de.hunar.insurance.shared.domain.Money;
import de.hunar.insurance.shared.web.ResourceNotFoundException;
import de.hunar.insurance.underwriting.UnderwritingApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Coordinates policy issuance and cancellation while using customer, product, and underwriting module APIs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService implements PolicyApi {
    private final PolicyRepository repository;
    private final CustomerApi customerApi;
    private final ProductApi productApi;
    private final UnderwritingApi underwritingApi;
    private final PolicyMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditTrailApi auditTrail;

    /**
     * Issues a policy when underwriting approves the customer-product combination.
     *
     * <p>Persists the policy, records its creation, and publishes a policy-issued event
     * in the current transaction.</p>
     *
     * @param request customer, product, and coverage-term input
     * @return the issued policy with customer and product details
     * @throws DomainException if underwriting rejects the request or the term is invalid
     * @throws ResourceNotFoundException if the customer or product does not exist
     */
    @Transactional
    public PolicyResponse issue(IssuePolicyRequest request) {
        var product = productApi.findById(request.productId());
        var customer = customerApi.findById(request.customerId());
        if (!underwritingApi.canIssue(request.customerId(), request.productId())) {
            throw new DomainException("Policy cannot be issued for the supplied customer and product");
        }
        if (!request.validTo().isAfter(request.validFrom())) {
            throw new DomainException("validTo must be after validFrom");
        }
        var policy = repository.save(new Policy(request.customerId(), request.productId(), request.validFrom(),
                request.validTo(), product.coverageLimit(), product.premium()));
        AuditContext context = auditTrail.captureContext();
        auditTrail.record("CREATED", "POLICY", policy.getId().toString(), "status=ACTIVE", context);
        log.info("Policy issued: policyId={} customerId={} productId={}",
                policy.getId(), policy.getCustomerId(), policy.getProductId());
        eventPublisher.publishEvent(new PolicyIssuedEvent(
                policy.getId(), policy.getCustomerId(), Money.eur(policy.getPremium()), context));
        log.info("Policy issued event published: policyId={} customerId={}",
                policy.getId(), policy.getCustomerId());
        return mapper.toResponse(policy, customer, product);
    }

    /**
     * Retrieves a policy response enriched with its customer and product details.
     *
     * @param id policy identifier
     * @return the policy response
     * @throws ResourceNotFoundException if the policy, customer, or product is missing
     */
    @Transactional(readOnly = true)
    public PolicyResponse findByIdResponse(Long id) {
        Policy policy = get(id);
        return mapper.toResponse(policy, customerApi.findById(policy.getCustomerId()),
                productApi.findById(policy.getProductId()));
    }

    /**
     * Lists policies enriched with their customer and product details.
     *
     * @return all persisted policies
     */
    @Transactional(readOnly = true)
    public List<PolicyResponse> findAllResponses() {
        List<Policy> policies = repository.findAll();
        Map<Long, CustomerSummary> customers = policies.stream()
                .map(Policy::getCustomerId)
                .distinct()
                .map(customerApi::findById)
                .collect(Collectors.toMap(CustomerSummary::id, Function.identity()));
        Map<Long, ProductSummary> products = policies.stream()
                .map(Policy::getProductId)
                .distinct()
                .map(productApi::findById)
                .collect(Collectors.toMap(ProductSummary::id, Function.identity()));
        return policies.stream()
                .map(policy -> mapper.toResponse(policy, customers.get(policy.getCustomerId()),
                        products.get(policy.getProductId())))
                .toList();
    }

    /**
     * Cancels an active policy and records the status change.
     *
     * @param id policy identifier
     * @return the cancelled policy response
     * @throws ResourceNotFoundException if the policy or its related customer/product is missing
     * @throws IllegalStateException if the policy is not active
     */
    @Transactional
    public PolicyResponse cancel(Long id) {
        Policy policy = get(id);
        PolicyStatus previousStatus = policy.getStatus();
        policy.cancel();
        Policy cancelledPolicy = repository.save(policy);
        auditTrail.record("STATUS_CHANGED", "POLICY", id.toString(),
                "from=" + previousStatus + ",to=" + cancelledPolicy.getStatus(), auditTrail.captureContext());
        log.info("Policy status changed: policyId={} fromStatus={} toStatus={}",
                id, previousStatus, cancelledPolicy.getStatus());
        return mapper.toResponse(cancelledPolicy, customerApi.findById(cancelledPolicy.getCustomerId()),
                productApi.findById(cancelledPolicy.getProductId()));
    }

    /**
     * Returns a policy snapshot for use by other modules.
     *
     * @param id policy identifier
     * @return policy data required by consuming modules
     * @throws ResourceNotFoundException if the policy or its customer cannot be found
     */
    @Override
    @Transactional(readOnly = true)
    public PolicySnapshot getSnapshot(Long id) {
        Policy policy = get(id);
        return mapper.toSnapshot(policy, customerApi.findById(policy.getCustomerId()));
    }

    /**
     * Returns snapshots for the requested policies, omitting identifiers that do not exist.
     *
     * @param ids policy identifiers to resolve
     * @return snapshots for the policies found
     */
    @Override
    @Transactional(readOnly = true)
    public List<PolicySnapshot> getSnapshots(Collection<Long> ids) {
        List<Policy> policies = repository.findAllById(ids);
        Map<Long, CustomerSummary> customers = policies.stream()
                .map(Policy::getCustomerId)
                .distinct()
                .map(customerApi::findById)
                .collect(Collectors.toMap(CustomerSummary::id, Function.identity()));
        return policies.stream()
                .map(policy -> mapper.toSnapshot(policy, customers.get(policy.getCustomerId())))
                .toList();
    }

    /**
     * Checks whether an active policy covers the supplied date, including both term boundaries.
     *
     * @param id policy identifier
     * @param date date on which coverage is evaluated
     * @return {@code true} if the policy is active and the date is within its term
     * @throws ResourceNotFoundException if the policy does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isActiveAt(Long id, LocalDate date) {
        Policy p = get(id);
        boolean active = p.getStatus() == PolicyStatus.ACTIVE
                && !date.isBefore(p.getValidFrom())
                && !date.isAfter(p.getValidTo());
        log.debug("Policy coverage checked: policyId={} date={} active={}", id, date, active);
        return active;
    }

    private Policy get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Policy", id));
    }
}
