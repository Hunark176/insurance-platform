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
import de.hunar.insurance.policy.events.PolicyCancelledEvent;
import de.hunar.insurance.policy.events.PolicyIssuedEvent;
import de.hunar.insurance.policy.mapper.PolicyMapper;
import de.hunar.insurance.product.ProductApi;
import de.hunar.insurance.product.ProductSummary;
import de.hunar.insurance.shared.domain.DomainException;
import de.hunar.insurance.shared.domain.Money;
import de.hunar.insurance.shared.web.PageResponse;
import de.hunar.insurance.shared.web.ResourceNotFoundException;
import de.hunar.insurance.underwriting.UnderwritingApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
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
    private final Clock clock;

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
        // Product terms are copied to the policy, so later product changes do not alter existing contracts.
        BigDecimal annualAggregateLimit = product.annualAggregateFactor() == null
                ? null
                : product.coverageLimit().multiply(BigDecimal.valueOf(product.annualAggregateFactor()));
        var policy = repository.save(new Policy(request.customerId(), request.productId(), request.validFrom(),
                request.validTo(), product.coverageLimit(), product.premium(), annualAggregateLimit));
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
     * Lists one page of policies enriched with their customer and product details.
     *
     * <p>Uses a constant number of queries regardless of the page size: page and count query for
     * the policies and one batch lookup each for customers and products.</p>
     *
     * @param pageable requested page and sort order
     * @return the requested page
     * @throws ResourceNotFoundException if a policy refers to a missing customer or product
     */
    @Transactional(readOnly = true)
    public PageResponse<PolicyResponse> findPage(Pageable pageable) {
        Page<Policy> page = repository.findAll(pageable);
        List<Policy> policies = page.getContent();
        Map<Long, CustomerSummary> customers = loadCustomers(policies);
        Map<Long, ProductSummary> products = byId(
                productApi.findByIds(policies.stream().map(Policy::getProductId).toList()), ProductSummary::id);
        return PageResponse.of(page, policies.stream()
                .map(policy -> mapper.toResponse(policy,
                        require(customers, policy.getCustomerId(), "Customer"),
                        require(products, policy.getProductId(), "Product")))
                .toList());
    }

    /**
     * Cancels an active policy and records the status change.
     *
     * <p>Losses up to and including the effective date stay covered; open claims are not
     * affected and continue through the normal claim process.</p>
     *
     * @param id policy identifier
     * @param effectiveOn last covered day; {@code null} means today
     * @return the cancelled policy response
     * @throws ResourceNotFoundException if the policy or its related customer/product is missing
     * @throws DomainException if the policy is not active or the date is invalid
     */
    @Transactional
    public PolicyResponse cancel(Long id, LocalDate effectiveOn) {
        Policy policy = get(id);
        PolicyStatus previousStatus = policy.getStatus();
        LocalDate today = LocalDate.now(clock);
        policy.cancel(effectiveOn == null ? today : effectiveOn, today);
        Policy cancelledPolicy = repository.save(policy);
        AuditContext context = auditTrail.captureContext();
        auditTrail.record("STATUS_CHANGED", "POLICY", id.toString(),
                "from=" + previousStatus + ",to=" + cancelledPolicy.getStatus()
                        + ",effectiveOn=" + cancelledPolicy.getCancelledOn(),
                context);
        log.info("Policy status changed: policyId={} fromStatus={} toStatus={} effectiveOn={}",
                id, previousStatus, cancelledPolicy.getStatus(), cancelledPolicy.getCancelledOn());
        // Billing credits the premium for the period after the cancellation (stored in the outbox).
        eventPublisher.publishEvent(new PolicyCancelledEvent(cancelledPolicy.getId(), cancelledPolicy.getCustomerId(),
                cancelledPolicy.getValidFrom(), cancelledPolicy.getValidTo(), cancelledPolicy.getCancelledOn(),
                Money.eur(cancelledPolicy.getPremium()), context));
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
        Map<Long, CustomerSummary> customers = loadCustomers(policies);
        return policies.stream()
                .map(policy -> mapper.toSnapshot(policy, require(customers, policy.getCustomerId(), "Customer")))
                .toList();
    }

    private Map<Long, CustomerSummary> loadCustomers(List<Policy> policies) {
        return byId(customerApi.findByIds(policies.stream().map(Policy::getCustomerId).toList()),
                CustomerSummary::id);
    }

    private static <T> Map<Long, T> byId(List<T> items, Function<T, Long> idOf) {
        return items.stream().collect(Collectors.toMap(idOf, Function.identity()));
    }

    /**
     * Keeps the previous behaviour of the single lookups: a dangling reference is reported as
     * not found instead of producing a response with missing data.
     */
    private static <T> T require(Map<Long, T> items, Long id, String type) {
        T item = items.get(id);
        if (item == null) {
            throw new ResourceNotFoundException(type, id);
        }
        return item;
    }

    /**
     * Checks whether a loss on the supplied date is covered, see {@link Policy#covers(LocalDate)}.
     *
     * @param id policy identifier
     * @param lossDate date on which the loss occurred
     * @return {@code true} if the date lies within the coverage period
     * @throws ResourceNotFoundException if the policy does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isCoveredOn(Long id, LocalDate lossDate) {
        boolean covered = get(id).covers(lossDate);
        log.debug("Policy coverage checked: policyId={} lossDate={} covered={}", id, lossDate, covered);
        return covered;
    }

    /**
     * Locks the policy row until the caller's transaction ends, see {@link PolicyApi#lockForClaimApproval}.
     *
     * @param id policy identifier
     * @return snapshot of the locked policy
     * @throws ResourceNotFoundException if the policy or its customer does not exist
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public PolicySnapshot lockForClaimApproval(Long id) {
        Policy policy = repository.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Policy", id));
        return mapper.toSnapshot(policy, customerApi.findById(policy.getCustomerId()));
    }

    private Policy get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Policy", id));
    }
}
