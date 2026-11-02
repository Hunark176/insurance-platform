package de.hunar.insurance.billing.application;

import de.hunar.insurance.audit.AuditContext;
import de.hunar.insurance.audit.AuditTrailApi;
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
    private final AuditTrailApi auditTrail;

    @ApplicationModuleListener
    void onClaimApproved(ClaimApprovedEvent event) {
        if (payoutRepository.existsByClaimId(event.claimId())) {
            log.info("Payout creation skipped because one already exists: claimId={}", event.claimId());
            return;
        }
        Payout payout = payoutRepository.save(Payout.create(event.claimId(), event.policyId(), event.amount()));
        AuditContext context = event.auditContext() == null ? auditTrail.captureContext() : event.auditContext();
        auditTrail.record("CREATED", "PAYOUT", payout.getId().toString(),
                "claimId=" + event.claimId() + ",policyId=" + event.policyId(), context);
        log.info("Payout created from claim approval: claimId={} policyId={}",
                event.claimId(), event.policyId());
    }
}
