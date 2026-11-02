package de.hunar.insurance.billing.application;

import de.hunar.insurance.billing.domain.PayoutRepository;
import de.hunar.insurance.audit.AuditTrailApi;
import de.hunar.insurance.audit.AuditContext;
import de.hunar.insurance.claim.events.ClaimApprovedEvent;
import de.hunar.insurance.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClaimApprovedListenerTest {
    private final PayoutRepository repository = mock(PayoutRepository.class);
    private final AuditTrailApi auditTrail = mock(AuditTrailApi.class);
    private final ClaimApprovedListener listener = new ClaimApprovedListener(repository, auditTrail);

    @Test
    void createsOnlyOnePayoutForAnApprovedClaim() {
        AuditContext context = new AuditContext("clerk", "request-1");
        ClaimApprovedEvent event = new ClaimApprovedEvent(
                7L, 11L, Money.eur(new BigDecimal("125.00")), context);
        when(repository.existsByClaimId(7L)).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> {
            var payout = (de.hunar.insurance.billing.domain.Payout) invocation.getArgument(0);
            ReflectionTestUtils.setField(payout, "id", 101L);
            return payout;
        });

        listener.onClaimApproved(event);

        verify(repository).save(any());
        ArgumentCaptor<de.hunar.insurance.billing.domain.Payout> payout = ArgumentCaptor.forClass(
                de.hunar.insurance.billing.domain.Payout.class
        );
        verify(repository).save(payout.capture());
        assertThat(payout.getValue().getClaimId()).isEqualTo(7L);
        verify(auditTrail).record("CREATED", "PAYOUT", "101", "claimId=7,policyId=11", context);
    }

    @Test
    void ignoresDuplicateEvent() {
        when(repository.existsByClaimId(7L)).thenReturn(true);

        listener.onClaimApproved(new ClaimApprovedEvent(7L, 11L, Money.eur(new BigDecimal("125.00"))));

        verify(repository, never()).save(any());
    }
}
