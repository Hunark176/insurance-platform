package de.hunar.insurance;

import de.hunar.insurance.claim.entity.Claim;
import de.hunar.insurance.claim.entity.ClaimType;
import de.hunar.insurance.claim.entity.ClaimStatus;
import de.hunar.insurance.claim.dto.ClaimResponse;
import de.hunar.insurance.claim.dto.CreateClaimRequest;
import de.hunar.insurance.claim.service.ClaimService;
import de.hunar.insurance.policy.application.PolicyService;
import de.hunar.insurance.policy.domain.Policy;
import de.hunar.insurance.policy.domain.PolicyRepository;
import de.hunar.insurance.shared.web.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class InsurancePlatformApplicationTests {

    @Autowired
    private ClaimService claimService;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void devProfileInitializesDemoPoliciesAndClaims() {
        assertThat(jdbcTemplate.queryForObject("select count(*) from customers", Integer.class))
                .isEqualTo(50);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from customers where customer_number = 'DEMO-1001'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from products", Integer.class))
                .isEqualTo(5);
        assertThat(jdbcTemplate.queryForObject("select count(*) from policies", Integer.class))
                .isEqualTo(65);
        assertThat(jdbcTemplate.queryForObject("select count(distinct product_id) from policies", Integer.class))
                .isEqualTo(5);
        assertThat(jdbcTemplate.queryForObject("select count(*) from invoices", Integer.class))
                .isEqualTo(65);
        assertThat(jdbcTemplate.queryForObject("select count(*) from claims", Integer.class))
                .isEqualTo(27);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from claims c join policies p on p.id = c.policy_id " +
                        "where c.occurred_on not between p.valid_from and p.valid_to", Integer.class))
                .isZero();
        assertThat(jdbcTemplate.queryForObject("select count(*) from payouts", Integer.class))
                .isEqualTo(12);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from products where code = 'DEMO-COMPREHENSIVE'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from policies p join customers c on c.id = p.customer_id " +
                        "where c.customer_number = 'DEMO-1001'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from claims cl join policies p on p.id = cl.policy_id " +
                        "join customers c on c.id = p.customer_id where c.customer_number = 'DEMO-1001'",
                Integer.class))
                .isEqualTo(3);
        assertThat(policyService.findAllResponses())
                .hasSize(65)
                .anySatisfy(policy -> {
                    assertThat(policy.customerName()).isEqualTo("Anna Schneider");
                    assertThat(policy.customerNumber()).isEqualTo("DEMO-1001");
                    assertThat(policy.productName()).isEqualTo("Privat-Komfortschutz");
                });
        assertThat(claimService.getClaimResponses(PageRequest.of(0, 2,
                        Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))), null)
                        .content())
                .hasSize(2)
                .allSatisfy(claim -> {
                    assertThat(claim.customerNumber()).isEqualTo("DEMO-1001");
                    assertThat(claim.customerName()).isEqualTo("Anna Schneider");
                });
    }

    @Test
    void claimOverviewIsPagedAndCanBeFilteredByPolicy() {
        Long policyId = jdbcTemplate.queryForObject(
                "select p.id from policies p join customers c on c.id = p.customer_id " +
                        "where c.customer_number = 'DEMO-1001'", Long.class);
        var pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id")));

        var allClaims = claimService.getClaimResponses(pageable, null);
        var policyClaims = claimService.getClaimResponses(pageable, policyId);

        assertThat(allClaims.content()).hasSize(2);
        assertThat(allClaims.totalElements()).isEqualTo(27);
        assertThat(allClaims.totalPages()).isEqualTo(14);
        assertThat(policyClaims.content()).hasSize(2)
                .allSatisfy(claim -> assertThat(claim.policyId()).isEqualTo(policyId));
        assertThat(policyClaims.totalElements()).isEqualTo(3);
        assertThat(claimService.getClaimResponses(pageable, Long.MAX_VALUE).totalElements()).isZero();
    }

    @Test
    void createClaim_setsStatusToReceived() {
        ClaimResponse savedClaim = claimService.createClaimResponse(createTestRequest("JUnit-Test: Claim anlegen"));

        assertThat(savedClaim.id()).isNotNull();
        assertThat(savedClaim.status()).isEqualTo(ClaimStatus.RECEIVED);
        assertThat(savedClaim.createdAt()).isNotNull();
        assertThat(savedClaim.occurredOn()).isEqualTo(LocalDate.now());
    }

    @Test
    void updateClaim_allowsReceivedToInReview() {
        // Given: Einen neuen Claim über die echte Service-Methode anlegen
        ClaimResponse savedClaim = claimService.createClaimResponse(createTestRequest("JUnit-Test: Statuswechsel"));

        // Kontrolle der Ausgangslage
        assertThat(savedClaim.status()).isEqualTo(ClaimStatus.RECEIVED);

        // When: Echte Methode updateClaim(...) aufrufen
        Claim updatedClaim = claimService.updateClaim(
                savedClaim.id(),
                ClaimStatus.IN_REVIEW
        );

        // Then: Der erlaubte Statuswechsel ist erfolgt
        assertThat(updatedClaim.getStatus())
                .isEqualTo(ClaimStatus.IN_REVIEW);
    }

	@Test
	void updateClaim_rejectsReceivedToApproved() {
		// Given: Einen neuen Claim anlegen
		ClaimResponse savedClaim = claimService.createClaimResponse(
                createTestRequest("JUnit-Test: verbotener Statuswechsel"));

		// Kontrolle: Neuer Claim startet bei RECEIVED
		assertThat(savedClaim.status())
				.isEqualTo(ClaimStatus.RECEIVED);

		// When + Then:
		// RECEIVED → APPROVED ist verboten und muss eine Exception werfen
		assertThatThrownBy(() ->
				claimService.updateClaim(
						savedClaim.id(),
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
		).isInstanceOf(ResourceNotFoundException.class)
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
		).isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Claim not found: 999998");
	}

    private CreateClaimRequest createTestRequest(String description) {
        TestPolicy policy = createTestPolicy();
        return new CreateClaimRequest(policy.id(), LocalDate.now(), policy.customerNumber(),
                ClaimType.AUTO, description, new BigDecimal("100.00"));
    }

    private TestPolicy createTestPolicy() {
        String suffix = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "insert into customers (customer_number, display_name, email, active) values (?, ?, ?, true)",
                "TEST-" + suffix, "Test Customer", suffix + "@example.test");
        Long customerId = jdbcTemplate.queryForObject(
                "select id from customers where customer_number = ?", Long.class, "TEST-" + suffix);
        jdbcTemplate.update(
                "insert into products (code, name, coverage_limit, premium, active) values (?, ?, ?, ?, true)",
                "TEST-" + suffix, "Test Product", new BigDecimal("100000.00"), new BigDecimal("10.00"));
        Long productId = jdbcTemplate.queryForObject(
                "select id from products where code = ?", Long.class, "TEST-" + suffix);
        Policy policy = policyRepository.save(new Policy(customerId, productId, LocalDate.now().minusDays(1),
                LocalDate.now().plusYears(1), new BigDecimal("100000.00"), new BigDecimal("10.00")));
        return new TestPolicy(policy.getId(), "TEST-" + suffix);
    }

    private record TestPolicy(Long id, String customerNumber) {}
}