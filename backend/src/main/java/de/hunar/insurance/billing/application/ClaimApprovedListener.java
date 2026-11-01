package de.hunar.insurance.billing.application;

import de.hunar.insurance.billing.domain.Payout;
import de.hunar.insurance.billing.domain.PayoutRepository;
import de.hunar.insurance.claim.events.ClaimApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
class ClaimApprovedListener {
    private final PayoutRepository payoutRepository;

    @ApplicationModuleListener
    void onClaimApproved(ClaimApprovedEvent event) {
        if (payoutRepository.existsByClaimId(event.claimId())) {
            log.info("Payout creation skipped because one already exists: claimId={}", event.claimId());
            return;
        }
        payoutRepository.save(Payout.create(event.claimId(), event.policyId(), event.amount()));
        log.info("Payout created from claim approval: claimId={} policyId={}",
                event.claimId(), event.policyId());
    }
}
