package de.hunar.insurance;

import de.hunar.insurance.claim.entity.Claim;
import de.hunar.insurance.claim.entity.ClaimStatus;
import de.hunar.insurance.claim.service.ClaimService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class InsurancePlatformApplicationTests {

    @Autowired
    private ClaimService claimService;

    @Test
    void contextLoads() {
    }

    @Test
    void createClaim_setsStatusToReceived() {
        // Given
        Claim claim = new Claim();
        claim.setCustomerNumber("TEST-1001");
        claim.setClaimType("TEST");
        claim.setDescription("JUnit-Test: Claim anlegen");
        claim.setAmount(new BigDecimal("100.00"));

        // When
        Claim savedClaim = claimService.createClaim(claim);

        // Then
        assertThat(savedClaim.getId()).isNotNull();
        assertThat(savedClaim.getStatus()).isEqualTo(ClaimStatus.RECEIVED);
        assertThat(savedClaim.getCreatedAt()).isNotNull();
    }

    @Test
    void updateClaim_allowsReceivedToInReview() {
        // Given: Einen neuen Claim über die echte Service-Methode anlegen
        Claim claim = new Claim();
        claim.setCustomerNumber("TEST-2001");
        claim.setClaimType("TEST");
        claim.setDescription("JUnit-Test: Statuswechsel");
        claim.setAmount(new BigDecimal("200.00"));

        Claim savedClaim = claimService.createClaim(claim);

        // Kontrolle der Ausgangslage
        assertThat(savedClaim.getStatus()).isEqualTo(ClaimStatus.RECEIVED);

        // When: Echte Methode updateClaim(...) aufrufen
        Claim updatedClaim = claimService.updateClaim(
                savedClaim.getId(),
                ClaimStatus.IN_REVIEW
        );

        // Then: Der erlaubte Statuswechsel ist erfolgt
        assertThat(updatedClaim.getStatus())
                .isEqualTo(ClaimStatus.IN_REVIEW);
    }

	@Test
	void updateClaim_rejectsReceivedToApproved() {
		// Given: Einen neuen Claim anlegen
		Claim claim = new Claim();
		claim.setCustomerNumber("TEST-3001");
		claim.setClaimType("TEST");
		claim.setDescription("JUnit-Test: verbotener Statuswechsel");
		claim.setAmount(new BigDecimal("300.00"));

		Claim savedClaim = claimService.createClaim(claim);

		// Kontrolle: Neuer Claim startet bei RECEIVED
		assertThat(savedClaim.getStatus())
				.isEqualTo(ClaimStatus.RECEIVED);

		// When + Then:
		// RECEIVED → APPROVED ist verboten und muss eine Exception werfen
		assertThatThrownBy(() ->
				claimService.updateClaim(
						savedClaim.getId(),
						ClaimStatus.APPROVED
				)
		).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Ungültiger Status-Übergang");
	}
	@Test
	void deleteClaim_throwsExceptionForNonExistingId() {
		// Given: Eine ID, die sicher nicht in der Datenbank existiert
		Long nonExistingId = 999_999L;

		// When + Then:
		// deleteClaim(...) muss bei dieser ID eine IllegalArgumentException werfen
		assertThatThrownBy(() ->
				claimService.deleteClaim(nonExistingId)
		).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Claim not found: 999999");
	}

	@Test
	void getClaimById_throwsExceptionForNonExistingId() {
		// Given: Eine ID, die sicher nicht existiert
		Long nonExistingId = 999_998L;

		// When + Then:
		// getClaimById(...) muss eine IllegalArgumentException werfen
		assertThatThrownBy(() ->
				claimService.getClaimById(nonExistingId)
		).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Claim not found: 999998");
	}
}