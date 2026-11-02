package de.hunar.insurance.billing.application;

import de.hunar.insurance.audit.AuditContext;
import de.hunar.insurance.audit.AuditTrailApi;
import de.hunar.insurance.billing.domain.Invoice;
import de.hunar.insurance.billing.domain.InvoiceRepository;
import de.hunar.insurance.policy.events.PolicyIssuedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
class PolicyIssuedListener {
    private final InvoiceRepository invoiceRepository;
    private final AuditTrailApi auditTrail;

    @ApplicationModuleListener
    void onPolicyIssued(PolicyIssuedEvent event) {
        if (invoiceRepository.existsByPolicyId(event.policyId())) {
            log.info("Invoice creation skipped because one already exists: policyId={}", event.policyId());
            return;
        }
        Invoice invoice = invoiceRepository.save(
                Invoice.create(event.policyId(), event.customerId(), event.premium()));
        AuditContext context = event.auditContext() == null ? auditTrail.captureContext() : event.auditContext();
        auditTrail.record("CREATED", "INVOICE", invoice.getId().toString(),
                "policyId=" + event.policyId(), context);
        log.info("Invoice created from policy issuance: policyId={} customerId={}",
                event.policyId(), event.customerId());
    }
}
