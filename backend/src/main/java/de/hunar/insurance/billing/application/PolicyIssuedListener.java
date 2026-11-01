package de.hunar.insurance.billing.application;

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

    @ApplicationModuleListener
    void onPolicyIssued(PolicyIssuedEvent event) {
        if (invoiceRepository.existsByPolicyId(event.policyId())) {
            log.info("Invoice creation skipped because one already exists: policyId={}", event.policyId());
            return;
        }
        invoiceRepository.save(Invoice.create(event.policyId(), event.customerId(), event.premium()));
        log.info("Invoice created from policy issuance: policyId={} customerId={}",
                event.policyId(), event.customerId());
    }
}
