package de.hunar.insurance.claim;

import de.hunar.insurance.claim.entity.Claim;
import de.hunar.insurance.claim.entity.ClaimStatus;
import de.hunar.insurance.claim.repository.ClaimRepository;
import de.hunar.insurance.claim.service.ClaimService;
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
        Claim claim = new Claim();
        claim.setStatus(null);

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Claim result = claimService.createClaim(claim);

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.RECEIVED);

        ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
        verify(claimRepository).save(claimCaptor.capture());
        assertThat(claimCaptor.getValue().getStatus()).isEqualTo(ClaimStatus.RECEIVED);
    }
}
