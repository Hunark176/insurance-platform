package de.hunar.insurance.claim.service;

import de.hunar.insurance.claim.entity.Claim;
import de.hunar.insurance.claim.entity.ClaimStatus;
import de.hunar.insurance.claim.dto.ClaimResponse;
import de.hunar.insurance.claim.dto.CreateClaimRequest;
import de.hunar.insurance.claim.events.ClaimApprovedEvent;
import de.hunar.insurance.claim.mapper.ClaimMapper;
import de.hunar.insurance.claim.repository.ClaimRepository;
import de.hunar.insurance.policy.PolicyApi;
import de.hunar.insurance.policy.PolicySnapshot;
import de.hunar.insurance.shared.domain.DomainException;
import de.hunar.insurance.shared.domain.Money;
import de.hunar.insurance.shared.web.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PolicyApi policyApi;
    private final ClaimMapper claimMapper;

    @Transactional(readOnly = true)
    public List<ClaimResponse> getAllClaimResponses() {
        List<Claim> claims = claimRepository.findAll();
        Map<Long, PolicySnapshot> policies = policyApi.getSnapshots(
                        claims.stream().map(Claim::getPolicyId).distinct().toList())
                .stream().collect(Collectors.toMap(PolicySnapshot::id, Function.identity()));
        return claims.stream().map(claim -> claimMapper.toResponse(claim, requirePolicy(policies, claim.getPolicyId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Claim getClaimById(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", id));
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaimResponse(Long id) {
        Claim claim = getClaimById(id);
        return claimMapper.toResponse(claim, policyApi.getSnapshot(claim.getPolicyId()));
    }

    public ClaimResponse createClaimResponse(CreateClaimRequest request) {
        if (request == null || request.policyId() == null) {
            throw new DomainException("Policy is required to report a claim");
        }
        if (request.occurredOn() == null || request.occurredOn().isAfter(LocalDate.now())) {
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
        claim.setStatus(ClaimStatus.RECEIVED);
        Claim saved = claimRepository.save(claim);
        return claimMapper.toResponse(saved, policy);
    }

    public Claim updateClaim(Long id, ClaimStatus newStatus) {
        Claim claim = getClaimById(id);
        validateStatusTransition(claim.getStatus(), newStatus);
        if (newStatus == ClaimStatus.APPROVED) {
            PolicySnapshot policy = policyApi.getSnapshot(claim.getPolicyId());
            if (!policyApi.isActiveAt(claim.getPolicyId(), claim.getOccurredOn())) {
                throw new DomainException("Policy did not provide coverage on the date of loss");
            }
            if (claim.getAmount().compareTo(policy.coverageLimit().amount()) > 0) {
                throw new DomainException("Claim amount exceeds the policy coverage limit");
            }
        }
        claim.setStatus(newStatus);
        Claim saved = claimRepository.save(claim);
        if (newStatus == ClaimStatus.APPROVED) {
            eventPublisher.publishEvent(new ClaimApprovedEvent(saved.getId(),
                    saved.getPolicyId(),
                    Money.eur(saved.getAmount())));
        }
        return saved;
    }

    public ClaimResponse updateClaimResponse(Long id, ClaimStatus newStatus) {
        Claim claim = updateClaim(id, newStatus);
        return claimMapper.toResponse(claim, policyApi.getSnapshot(claim.getPolicyId()));
    }

    public void deleteClaim(Long id) {
        if (!claimRepository.existsById(id)) {
            throw new ResourceNotFoundException("Claim", id);
        }
        claimRepository.deleteById(id);
    }

    private void validateStatusTransition(ClaimStatus current, ClaimStatus target) {
        if (target == null) {
            throw new DomainException("Claim status is required");
        }
        boolean valid = switch (current) {
            case RECEIVED -> target == ClaimStatus.IN_REVIEW;
            case IN_REVIEW -> target == ClaimStatus.APPROVED || target == ClaimStatus.REJECTED;
            case APPROVED, REJECTED -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    "Ungültiger Status-Übergang: " + current + " → " + target);
        }
    }

    private PolicySnapshot requirePolicy(Map<Long, PolicySnapshot> policies, Long policyId) {
        PolicySnapshot policy = policies.get(policyId);
        if (policy == null) {
            throw new ResourceNotFoundException("Policy", policyId);
        }
        return policy;
    }
}
