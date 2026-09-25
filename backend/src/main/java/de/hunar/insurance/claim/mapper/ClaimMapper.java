package de.hunar.insurance.claim.mapper;

import de.hunar.insurance.claim.dto.ClaimResponse;
import de.hunar.insurance.claim.dto.CreateClaimRequest;
import de.hunar.insurance.claim.entity.Claim;
import org.springframework.stereotype.Component;

@Component
public class ClaimMapper {
    public Claim toEntity(CreateClaimRequest request) {
        Claim claim = new Claim();
        claim.setPolicyId(request.policyId());
        claim.setCustomerNumber(request.customerNumber());
        claim.setClaimType(request.claimType());
        claim.setDescription(request.description());
        claim.setAmount(request.amount());
        return claim;
    }

    public ClaimResponse toResponse(Claim claim) {
        return new ClaimResponse(claim.getId(), claim.getPolicyId(), claim.getCustomerNumber(), claim.getClaimType(),
                claim.getDescription(), claim.getAmount(), claim.getStatus(), claim.getCreatedAt());
    }
}
