package de.hunar.insurance.billing.application;

import de.hunar.insurance.audit.AuditContext;
import de.hunar.insurance.audit.AuditTrailApi;
import de.hunar.insurance.billing.domain.CreditNote;
import de.hunar.insurance.billing.domain.CreditNoteRepository;
import de.hunar.insurance.policy.events.PolicyCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * Credits the premium for the period after a policy cancellation (§ 39 VVG).
 *
 * <p>Idempotent: a repeated event finds the existing credit note; {@code credit_notes.policy_id}
 * is unique as a second safeguard.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
class PolicyCancelledListener {
    private final CreditNoteRepository creditNotes;
    private final AuditTrailApi auditTrail;
    private final Clock clock;

    @ApplicationModuleListener
    void onPolicyCancelled(PolicyCancelledEvent event) {
        if (creditNotes.existsByPolicyId(event.policyId())) {
            log.info("Credit note creation skipped because one already exists: policyId={}", event.policyId());
            return;
        }
        CreditNote.forCancellation(event.policyId(), event.customerId(), event.premium(), event.validFrom(),
                        event.validTo(), event.cancelledOn(), clock.instant())
                .ifPresentOrElse(note -> {
                    CreditNote saved = creditNotes.save(note);
                    AuditContext context = event.auditContext() == null
                            ? auditTrail.captureContext() : event.auditContext();
                    auditTrail.record("CREATED", "CREDIT_NOTE", saved.getId().toString(),
                            "policyId=" + event.policyId() + ",days=" + saved.getCreditedDays() + "/"
                                    + saved.getTermDays(), context);
                    log.info("Credit note created after cancellation: policyId={} creditedDays={} termDays={}",
                            event.policyId(), saved.getCreditedDays(), saved.getTermDays());
                }, () -> log.info("No credit note needed: policyId={} cancelledOn={} validTo={}",
                        event.policyId(), event.cancelledOn(), event.validTo()));
    }
}
