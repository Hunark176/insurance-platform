package de.hunar.insurance.billing.application;

import de.hunar.insurance.billing.domain.PayoutRepository;
import de.hunar.insurance.claim.events.ClaimApprovedEvent;
import de.hunar.insurance.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClaimApprovedListenerTest {
    private final PayoutRepository repository = mock(PayoutRepository.class);
    private final ClaimApprovedListener listener = new ClaimApprovedListener(repository);

    @Test
    void createsOnlyOnePayoutForAnApprovedClaim() {
        ClaimApprovedEvent event = new ClaimApprovedEvent(7L, 11L, Money.eur(new BigDecimal("125.00")));
        when(repository.existsByClaimId(7L)).thenReturn(false);

        listener.onClaimApproved(event);

        verify(repository).save(any());
        ArgumentCaptor<de.hunar.insurance.billing.domain.Payout> payout = ArgumentCaptor.forClass(
                de.hunar.insurance.billing.domain.Payout.class
        );
        verify(repository).save(payout.capture());
        assertThat(payout.getValue().getClaimId()).isEqualTo(7L);
    }

    @Test
    void ignoresDuplicateEvent() {
        when(repository.existsByClaimId(7L)).thenReturn(true);

        listener.onClaimApproved(new ClaimApprovedEvent(7L, 11L, Money.eur(new BigDecimal("125.00"))));

        verify(repository, never()).save(any());
    }
}
