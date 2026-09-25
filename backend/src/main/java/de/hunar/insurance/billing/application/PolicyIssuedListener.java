package de.hunar.insurance.billing.application;

import de.hunar.insurance.billing.domain.Invoice;
import de.hunar.insurance.billing.domain.InvoiceRepository;
import de.hunar.insurance.policy.events.PolicyIssuedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PolicyIssuedListener {
    private final InvoiceRepository invoiceRepository;

    @ApplicationModuleListener
    void onPolicyIssued(PolicyIssuedEvent event) {
        if (!invoiceRepository.existsByPolicyId(event.policyId())) {
            invoiceRepository.save(Invoice.create(event.policyId(), event.customerId(), event.premium()));
        }
    }
}
