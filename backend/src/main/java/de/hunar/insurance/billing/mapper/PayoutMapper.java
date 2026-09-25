package de.hunar.insurance.billing.mapper;

import de.hunar.insurance.billing.domain.Payout;
import de.hunar.insurance.billing.dto.PayoutResponse;
import org.springframework.stereotype.Component;

@Component
public class PayoutMapper {
    public PayoutResponse toResponse(Payout payout) {
        return new PayoutResponse(payout.getId(), payout.getClaimId(), payout.getPolicyId(), payout.getAmount(),
                payout.getCurrency(), payout.getStatus());
    }
}
