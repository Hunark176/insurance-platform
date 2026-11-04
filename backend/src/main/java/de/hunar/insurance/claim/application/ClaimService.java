package de.hunar.insurance.claim.application;

import de.hunar.insurance.claim.domain.Claim;
import de.hunar.insurance.audit.AuditTrailApi;
import de.hunar.insurance.claim.domain.ClaimRepository;
import de.hunar.insurance.claim.domain.ClaimStatus;
import de.hunar.insurance.claim.dto.ClaimPageResponse;
import de.hunar.insurance.claim.dto.ClaimResponse;
import de.hunar.insurance.claim.dto.CreateClaimRequest;
import de.hunar.insurance.claim.events.ClaimApprovedEvent;
import de.hunar.insurance.claim.mapper.ClaimMapper;
import de.hunar.insurance.policy.PolicyApi;
import de.hunar.insurance.policy.PolicySnapshot;
import de.hunar.insurance.shared.domain.DomainException;
import de.hunar.insurance.shared.domain.Money;
import de.hunar.insurance.shared.web.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.Clock;

/**
 * Coordinates claim use cases and their policy checks, persistence, audit records, and domain events.
 *
 * <p>Policy-specific business data is obtained through {@link PolicyApi}; this service does not
 * access policy persistence directly.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PolicyApi policyApi;
    private final ClaimMapper claimMapper;
    private final Clock clock;
    private final AuditTrailApi auditTrail;

    /**
     * Returns a page of claims enriched with the corresponding policy snapshots.
     *
     * @param pageable requested page and sort order
     * @param policyId optional policy filter; {@code null} returns claims for all policies
     * @return the requested page with paging metadata
     * @throws ResourceNotFoundException if a claim refers to a policy that cannot be resolved
     */
    @Transactional(readOnly = true)
    public ClaimPageResponse getClaimResponses(Pageable pageable, Long policyId) {
        Page<Claim> claimPage = policyId == null
                ? claimRepository.findAll(pageable)
                : claimRepository.findByPolicyId(policyId, pageable);
        List<Claim> claims = claimPage.getContent();
        Map<Long, PolicySnapshot> policies = policyApi.getSnapshots(
                        claims.stream().map(Claim::getPolicyId).distinct().toList())
                .stream().collect(Collectors.toMap(PolicySnapshot::id, Function.identity())); System.out.println("policies" +
                "  "+policies);
        List<ClaimResponse> content = claims.stream()
                .map(claim -> claimMapper.toResponse(claim, requirePolicy(policies, claim.getPolicyId())))
                .toList();
        System.out.println("content" +
                "  "+content);
        return new ClaimPageResponse(content, claimPage.getNumber(), claimPage.getSize(),
                claimPage.getTotalElements(), claimPage.getTotalPages(), claimPage.isFirst(), claimPage.isLast());
    }

    /**
     * Loads a claim by its identifier.
     *
     * @param id claim identifier
     * @return the persisted claim
     * @throws ResourceNotFoundException if no claim has the given identifier
     */
    @Transactional(readOnly = true)
    public Claim getClaimById(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", id));
    }

    /**
     * Loads a claim and maps it to the API response using its policy snapshot.
     *
     * @param id claim identifier
     * @return the claim response
     * @throws ResourceNotFoundException if the claim or its policy cannot be found
     */
    @Transactional(readOnly = true)
    public ClaimResponse getClaimResponse(Long id) {
        Claim claim = getClaimById(id);
        return claimMapper.toResponse(claim, policyApi.getSnapshot(claim.getPolicyId()));
    }

    /**
     * Validates coverage and customer ownership before creating and auditing a claim.
     *
     * @param request validated claim input
     * @return response containing the newly persisted claim
     * @throws DomainException if the request violates claim or policy business rules
     * @throws ResourceNotFoundException if the referenced policy cannot be found
     */
    public ClaimResponse createClaimResponse(CreateClaimRequest request) {
        if (request == null || request.policyId() == null) {
            throw new DomainException("Policy is required to report a claim");
        }
        if (request.occurredOn() == null || request.occurredOn().isAfter(LocalDate.now(clock))) {
            throw new DomainException("Loss date must be today or in the past");
        }
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new DomainException("Claim amount must be greater than zero");
        }
        PolicySnapshot policy = policyApi.getSnapshot(request.policyId());
        if (!policy.customerNumber().equals(request.customerNumber())) {
            throw new DomainException("Customer does not match the supplied policy");
        }
        if (!policyApi.isActiveAt(request.policyId(), request.occurredOn())) {
            throw new DomainException("Policy did not provide coverage on the date of loss");
        }
        Claim claim = claimMapper.toEntity(request);
        Claim saved = claimRepository.save(claim);
        auditTrail.record("CREATED", "CLAIM", saved.getId().toString(), "status=RECEIVED",
                auditTrail.captureContext());
        log.info("Claim created: claimId={} policyId={} status={}",
                saved.getId(), saved.getPolicyId(), saved.getStatus());
        return claimMapper.toResponse(saved, policy);
    }

    /**
     * Changes a claim's status and publishes a claim-approved event when appropriate.
     *
     * <p>Approval additionally requires active coverage on the loss date and an amount
     * within the policy coverage limit.</p>
     *
     * @param id claim identifier
     * @param newStatus requested status
     * @return the updated persisted claim
     * @throws ResourceNotFoundException if the claim or its policy cannot be found
     * @throws DomainException if approval violates policy coverage rules
     * @throws IllegalStateException if the requested status transition is not allowed
     */
    public Claim updateClaim(Long id, ClaimStatus newStatus) {
        Claim claim = getClaimById(id);
        ClaimStatus previousStatus = claim.getStatus();
        claim.assertCanTransitionTo(newStatus);
        if (newStatus == ClaimStatus.APPROVED) {
            PolicySnapshot policy = policyApi.getSnapshot(claim.getPolicyId());
            if (!policyApi.isActiveAt(claim.getPolicyId(), claim.getOccurredOn())) {
                throw new DomainException("Policy did not provide coverage on the date of loss");
            }
            if (claim.getAmount().compareTo(policy.coverageLimit().amount()) > 0) {
                throw new DomainException("Claim amount exceeds the policy coverage limit");
            }
        }
        claim.transitionTo(newStatus);
        Claim saved = claimRepository.save(claim);
        auditTrail.record("STATUS_CHANGED", "CLAIM", saved.getId().toString(),
                "from=" + previousStatus + ",to=" + newStatus, auditTrail.captureContext());
        log.info("Claim status changed: claimId={} policyId={} fromStatus={} toStatus={}",
                saved.getId(), saved.getPolicyId(), previousStatus, newStatus);
        if (newStatus == ClaimStatus.APPROVED) {
            eventPublisher.publishEvent(new ClaimApprovedEvent(saved.getId(),
                    saved.getPolicyId(),
                    Money.eur(saved.getAmount()),
                    auditTrail.captureContext()));
            log.info("Claim approved event published: claimId={} policyId={}",
                    saved.getId(), saved.getPolicyId());
        }
        return saved;
    }

    /**
     * Updates a claim status and returns the mapped API representation.
     *
     * @param id claim identifier
     * @param newStatus requested status
     * @return the updated claim response
     * @throws ResourceNotFoundException if the claim or its policy cannot be found
     * @throws DomainException if a business rule rejects the update
     * @throws IllegalStateException if the requested status transition is not allowed
     */
    public ClaimResponse updateClaimResponse(Long id, ClaimStatus newStatus) {
        Claim claim = updateClaim(id, newStatus);
        return claimMapper.toResponse(claim, policyApi.getSnapshot(claim.getPolicyId()));
    }

    /**
     * Deletes an existing claim and records the deletion in the audit trail.
     *
     * @param id identifier of the claim to delete
     * @throws ResourceNotFoundException if no claim has the given identifier
     */
    public void deleteClaim(Long id) {
        if (!claimRepository.existsById(id)) {
            throw new ResourceNotFoundException("Claim", id);
        }
        claimRepository.deleteById(id);
        auditTrail.record("DELETED", "CLAIM", id.toString(), "claim deleted", auditTrail.captureContext());
        log.info("Claim deleted: claimId={}", id);
    }

    private PolicySnapshot requirePolicy(Map<Long, PolicySnapshot> policies, Long policyId) {
        PolicySnapshot policy = policies.get(policyId);
        if (policy == null) {
            throw new ResourceNotFoundException("Policy", policyId);
        }
        return policy;
    }
}
