package de.hunar.insurance.claim.mapper;

import de.hunar.insurance.claim.dto.ClaimResponse;
import de.hunar.insurance.claim.dto.CreateClaimRequest;
import de.hunar.insurance.claim.entity.Claim;
import de.hunar.insurance.policy.PolicySnapshot;
import org.springframework.stereotype.Component;

@Component
public class ClaimMapper {
    public Claim toEntity(CreateClaimRequest request) {
        Claim claim = new Claim();
        claim.setPolicyId(request.policyId());
        claim.setOccurredOn(request.occurredOn());
        claim.setClaimType(request.claimType());
        claim.setDescription(request.description());
        claim.setAmount(request.amount());
        return claim;
    }

    public ClaimResponse toResponse(Claim claim, PolicySnapshot policy) {
        return new ClaimResponse(claim.getId(), policy.id(), policy.customerNumber(), claim.getClaimType().name(),
                claim.getDescription(), claim.getAmount(), claim.getStatus(), claim.getCreatedAt(),
                claim.getOccurredOn());
    }
}
