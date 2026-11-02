package de.hunar.insurance.billing.application;

import de.hunar.insurance.audit.AuditTrailApi;
import de.hunar.insurance.billing.domain.Invoice;
import de.hunar.insurance.billing.domain.InvoiceRepository;
import de.hunar.insurance.policy.events.PolicyIssuedEvent;
import de.hunar.insurance.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PolicyIssuedListenerTest {
    private final InvoiceRepository repository = mock(InvoiceRepository.class);
    private final AuditTrailApi auditTrail = mock(AuditTrailApi.class);
    private final PolicyIssuedListener listener = new PolicyIssuedListener(repository, auditTrail);

    @Test
    void createsInvoiceAndAuditsItsGeneratedId() {
        when(repository.existsByPolicyId(11L)).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            ReflectionTestUtils.setField(invoice, "id", 101L);
            return invoice;
        });

        listener.onPolicyIssued(new PolicyIssuedEvent(
                11L, 7L, Money.eur(new BigDecimal("125.00"))));

        verify(repository).save(any());
        verify(auditTrail).record(
                "CREATED", "INVOICE", "101", "policyId=11", null);
    }

    @Test
    void ignoresDuplicateEvent() {
        when(repository.existsByPolicyId(11L)).thenReturn(true);

        listener.onPolicyIssued(new PolicyIssuedEvent(
                11L, 7L, Money.eur(new BigDecimal("125.00"))));

        verify(repository, never()).save(any());
        verify(auditTrail, never()).record(any(), any(), any(), any(), any());
    }
}
