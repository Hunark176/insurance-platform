package service;

import de.hunar.claim_tracker.entity.Claim;
import de.hunar.claim_tracker.entity.ClaimStatus;
import de.hunar.claim_tracker.repository.ClaimRepository;
import de.hunar.claim_tracker.service.ClaimService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @InjectMocks
    private ClaimService claimService;

    @Test
    void createClaim_setsStatusToReceived() {
        // Given: Ein neuer Claim ohne gesetzten Status
        Claim claim = new Claim();
        claim.setStatus(null);

        // Repository simulieren:
        // save(...) gibt den Claim zurück, den es erhalten hat.
        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When: Die echte Business-Methode aufrufen
        Claim result = claimService.createClaim(claim);

        // Then: Business-Regel prüfen
        assertThat(result.getStatus())
                .isEqualTo(ClaimStatus.RECEIVED);

        // Zusätzlich prüfen: Der Claim wurde gespeichert
        ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
        verify(claimRepository).save(claimCaptor.capture());

        assertThat(claimCaptor.getValue().getStatus())
                .isEqualTo(ClaimStatus.RECEIVED);
    }
}