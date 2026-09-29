package de.hunar.insurance.claim;

import de.hunar.insurance.claim.dto.ClaimResponse;
import de.hunar.insurance.claim.dto.ClaimPageResponse;
import de.hunar.insurance.claim.dto.CreateClaimRequest;
import de.hunar.insurance.claim.entity.Claim;
import de.hunar.insurance.claim.entity.ClaimStatus;
import de.hunar.insurance.claim.entity.ClaimType;
import de.hunar.insurance.claim.events.ClaimApprovedEvent;
import de.hunar.insurance.claim.mapper.ClaimMapper;
import de.hunar.insurance.claim.repository.ClaimRepository;
import de.hunar.insurance.claim.service.ClaimService;
import de.hunar.insurance.policy.PolicyApi;
import de.hunar.insurance.policy.PolicySnapshot;
import de.hunar.insurance.shared.domain.DomainException;
import de.hunar.insurance.shared.domain.Money;
import de.hunar.insurance.shared.web.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private PolicyApi policyApi;
    @Mock
    private ClaimMapper claimMapper;

    private ClaimService claimService;
    private final LocalDate lossDate = LocalDate.of(2026, 9, 20);
    private final PolicySnapshot policy = new PolicySnapshot(10L, 20L, "C-20", 30L,
            lossDate.minusMonths(1), lossDate.plusMonths(1),
            Money.eur(new BigDecimal("100000.00")));

    @BeforeEach
    void setUp() {
        claimService = new ClaimService(claimRepository, eventPublisher, policyApi, claimMapper);
    }

    @Test
    void createClaimSetsReceivedAndRequiresMatchingActivePolicy() {
        CreateClaimRequest request = validRequest();
        Claim claim = new Claim();
        claim.setPolicyId(policy.id());
        claim.setOccurredOn(lossDate);
        claim.setClaimType(request.claimType());
        claim.setAmount(request.amount());
        when(policyApi.getSnapshot(policy.id())).thenReturn(policy);
        when(policyApi.isActiveAt(policy.id(), lossDate)).thenReturn(true);
        when(claimMapper.toEntity(request)).thenReturn(claim);
        when(claimRepository.save(claim)).thenReturn(claim);
        when(claimMapper.toResponse(claim, policy)).thenReturn(
                new ClaimResponse(1L, policy.id(), policy.customerNumber(), "AUTO", request.description(),
                        request.amount(), ClaimStatus.RECEIVED, null, lossDate));

        ClaimResponse response = claimService.createClaimResponse(request);

        ArgumentCaptor<Claim> savedClaim = ArgumentCaptor.forClass(Claim.class);
        verify(claimRepository).save(savedClaim.capture());
        assertThat(savedClaim.getValue().getPolicyId()).isEqualTo(policy.id());
        assertThat(savedClaim.getValue().getOccurredOn()).isEqualTo(lossDate);
        assertThat(savedClaim.getValue().getStatus()).isEqualTo(ClaimStatus.RECEIVED);
        assertThat(response.status()).isEqualTo(ClaimStatus.RECEIVED);
    }

    @Test
    void getClaimResponsesFiltersAndMapsOnlyTheRequestedPage() {
        Pageable pageable = PageRequest.of(1, 2, Sort.by("createdAt").descending());
        Claim claim = reviewClaim(new BigDecimal("250.00"));
        when(claimRepository.findByPolicyId(policy.id(), pageable))
                .thenReturn(new PageImpl<>(List.of(claim), pageable, 5));
        when(policyApi.getSnapshots(List.of(policy.id()))).thenReturn(List.of(policy));
        ClaimResponse response = new ClaimResponse(7L, policy.id(), policy.customerNumber(), "AUTO", "Loss",
                claim.getAmount(), ClaimStatus.IN_REVIEW, null, lossDate);
        when(claimMapper.toResponse(claim, policy)).thenReturn(response);

        ClaimPageResponse result = claimService.getClaimResponses(pageable, policy.id());

        assertThat(result.content()).containsExactly(response);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.first()).isFalse();
        assertThat(result.last()).isFalse();
        verify(claimRepository).findByPolicyId(policy.id(), pageable);
        verify(claimRepository, never()).findAll(any(Pageable.class));
        verify(policyApi).getSnapshots(List.of(policy.id()));
    }

    @Test
    void createClaimRejectsCustomerThatDoesNotOwnPolicy() {
        CreateClaimRequest request = new CreateClaimRequest(policy.id(), lossDate, "OTHER-CUSTOMER",
                ClaimType.AUTO, "Loss", new BigDecimal("100.00"));
        when(policyApi.getSnapshot(policy.id())).thenReturn(policy);

        assertThatThrownBy(() -> claimService.createClaimResponse(request))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Customer does not match");
        verify(policyApi, never()).isActiveAt(any(), any());
        verifyNoInteractions(claimRepository);
    }

    @Test
    void createClaimRejectsLossOutsidePolicyCoveragePeriod() {
        CreateClaimRequest request = validRequest();
        when(policyApi.getSnapshot(policy.id())).thenReturn(policy);
        when(policyApi.isActiveAt(policy.id(), lossDate)).thenReturn(false);

        assertThatThrownBy(() -> claimService.createClaimResponse(request))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("did not provide coverage");
        verifyNoInteractions(claimRepository);
    }

    @Test
    void statusTransitionRequiresClaimToExist() {
        when(claimRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> claimService.updateClaim(404L, ClaimStatus.IN_REVIEW))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Claim not found");
    }

    @Test
    void approvalRequiresCoveredLossAndAmountWithinLimit() {
        Claim claim = reviewClaim(new BigDecimal("500.00"));
        when(claimRepository.findById(7L)).thenReturn(Optional.of(claim));
        when(policyApi.getSnapshot(policy.id())).thenReturn(policy);
        when(policyApi.isActiveAt(policy.id(), lossDate)).thenReturn(true);
        when(claimRepository.save(claim)).thenReturn(claim);

        Claim result = claimService.updateClaim(7L, ClaimStatus.APPROVED);

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.APPROVED);
        ArgumentCaptor<ClaimApprovedEvent> event = ArgumentCaptor.forClass(ClaimApprovedEvent.class);
        verify(eventPublisher).publishEvent((Object) event.capture());
        assertThat(event.getValue().claimId()).isEqualTo(7L);
        assertThat(event.getValue().policyId()).isEqualTo(policy.id());
    }

    @Test
    void approvalRejectsAmountAboveCoverageLimit() {
        Claim claim = reviewClaim(new BigDecimal("100000.01"));
        when(claimRepository.findById(7L)).thenReturn(Optional.of(claim));
        when(policyApi.getSnapshot(policy.id())).thenReturn(policy);
        when(policyApi.isActiveAt(policy.id(), lossDate)).thenReturn(true);

        assertThatThrownBy(() -> claimService.updateClaim(7L, ClaimStatus.APPROVED))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("exceeds the policy coverage limit");
        verify(claimRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void approvalRejectsPolicyWithoutCoverageAtLossDate() {
        Claim claim = reviewClaim(new BigDecimal("500.00"));
        when(claimRepository.findById(7L)).thenReturn(Optional.of(claim));
        when(policyApi.getSnapshot(policy.id())).thenReturn(policy);
        when(policyApi.isActiveAt(policy.id(), lossDate)).thenReturn(false);

        assertThatThrownBy(() -> claimService.updateClaim(7L, ClaimStatus.APPROVED))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("did not provide coverage");
        verify(claimRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    private CreateClaimRequest validRequest() {
        return new CreateClaimRequest(policy.id(), lossDate, policy.customerNumber(), ClaimType.AUTO,
                "Parking damage", new BigDecimal("100.00"));
    }

    private Claim reviewClaim(BigDecimal amount) {
        Claim claim = new Claim();
        claim.setId(7L);
        claim.setPolicyId(policy.id());
        claim.setOccurredOn(lossDate);
        claim.setClaimType(ClaimType.AUTO);
        claim.setAmount(amount);
        claim.setStatus(ClaimStatus.IN_REVIEW);
        return claim;
    }
}
