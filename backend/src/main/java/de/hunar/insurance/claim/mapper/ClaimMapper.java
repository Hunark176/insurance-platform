package de.hunar.insurance.claim.mapper;

import de.hunar.insurance.claim.dto.ClaimResponse;
import de.hunar.insurance.claim.dto.CreateClaimRequest;
import de.hunar.insurance.claim.domain.Claim;
import de.hunar.insurance.policy.PolicySnapshot;
import org.springframework.stereotype.Component;

@Component
public class ClaimMapper {
    public Claim toEntity(CreateClaimRequest request) {
        return Claim.create(
                request.policyId(),
                request.occurredOn(),
                request.claimType(),
                request.description(),
                request.amount()
        );
    }

    public ClaimResponse toResponse(Claim claim, PolicySnapshot policy) {
        return new ClaimResponse(claim.getId(), policy.id(), policy.customerNumber(), policy.customerName(),
                claim.getClaimType().name(), claim.getDescription(), claim.getAmount(), claim.getStatus(),
                claim.getCreatedAt(), claim.getOccurredOn());
    }
}
