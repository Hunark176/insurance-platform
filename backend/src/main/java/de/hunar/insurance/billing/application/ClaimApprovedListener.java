package de.hunar.insurance.billing.application;

import de.hunar.insurance.billing.domain.Payout;
import de.hunar.insurance.billing.domain.PayoutRepository;
import de.hunar.insurance.claim.events.ClaimApprovedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ClaimApprovedListener {
    private final PayoutRepository payoutRepository;

    @ApplicationModuleListener
    void onClaimApproved(ClaimApprovedEvent event) {
        if (!payoutRepository.existsByClaimId(event.claimId())) {
            payoutRepository.save(Payout.create(event.claimId(), event.policyId(), event.amount()));
        }
    }
}
