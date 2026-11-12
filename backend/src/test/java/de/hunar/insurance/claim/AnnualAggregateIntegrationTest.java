package de.hunar.insurance.claim;

import de.hunar.insurance.claim.application.ClaimService;
import de.hunar.insurance.claim.domain.ClaimRepository;
import de.hunar.insurance.claim.domain.ClaimStatus;
import de.hunar.insurance.claim.domain.ClaimType;
import de.hunar.insurance.claim.dto.CreateClaimRequest;
import de.hunar.insurance.policy.domain.Policy;
import de.hunar.insurance.policy.domain.PolicyRepository;
import de.hunar.insurance.shared.domain.DomainException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Annual aggregate against a real database: claims of one policy year together may not exceed
 * the limit, also when they are approved at the same time.
 *
 * <p>Deliberately not transactional: every approval commits on its own, as in production.</p>
 */
@SpringBootTest
@TestPropertySource(properties = {
        "insurance.dev.h2.tcp.enabled=false",
        "insurance.events.maintenance.enabled=false"
})
class AnnualAggregateIntegrationTest {

    /** Per loss 100,000 EUR, per policy year 150,000 EUR in total. */
    private static final BigDecimal COVERAGE_LIMIT = new BigDecimal("100000.00");
    private static final BigDecimal ANNUAL_AGGREGATE = new BigDecimal("150000.00");

    @Autowired
    private ClaimService claimService;
    @Autowired
    private ClaimRepository claimRepository;
    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void claimsOfOnePolicyYearMayNotExceedTheAnnualAggregateTogether() {
        TestPolicy policy = createLiabilityPolicy();
        Long first = claimInReview(policy, "90000.00");
        Long second = claimInReview(policy, "70000.00");
        Long third = claimInReview(policy, "60000.00");

        claimService.updateClaim(first, ClaimStatus.APPROVED);

        // Each claim is within the per-loss limit, but 90,000 + 70,000 exceeds 150,000.
        assertThatThrownBy(() -> claimService.updateClaim(second, ClaimStatus.APPROVED))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("60000.00 EUR left");
        assertThat(claimService.updateClaim(third, ClaimStatus.APPROVED).getStatus())
                .isEqualTo(ClaimStatus.APPROVED);
        assertThat(claimRepository.findById(second).orElseThrow().getStatus()).isEqualTo(ClaimStatus.IN_REVIEW);
    }

    @Test
    void concurrentApprovalsCannotTogetherExceedTheAnnualAggregate() throws Exception {
        TestPolicy policy = createLiabilityPolicy();
        Long first = claimInReview(policy, "90000.00");
        Long second = claimInReview(policy, "70000.00");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (Long claimId : List.of(first, second)) {
                results.add(executor.submit(() -> {
                    start.await();
                    try {
                        claimService.updateClaim(claimId, ClaimStatus.APPROVED);
                        return true;
                    } catch (DomainException rejected) {
                        return false;
                    }
                }));
            }
            start.countDown();

            int approved = 0;
            for (Future<Boolean> result : results) {
                if (result.get(30, TimeUnit.SECONDS)) {
                    approved++;
                }
            }

            // Without the policy lock both approvals could read the same (empty) sum and pass.
            assertThat(approved).isEqualTo(1);
            assertThat(approvedTotal(policy)).isLessThanOrEqualTo(ANNUAL_AGGREGATE);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void newPolicyYearStartsWithTheFullAnnualAggregate() {
        // Policy started 13 months ago: last year's losses do not reduce this year's aggregate.
        TestPolicy policy = createLiabilityPolicy(LocalDate.now().minusMonths(13));
        Long lastYear = claimInReview(policy, "100000.00", LocalDate.now().minusMonths(12).minusDays(5));
        Long lastYearToo = claimInReview(policy, "50000.00", LocalDate.now().minusMonths(12).minusDays(4));
        Long thisYear = claimInReview(policy, "100000.00", LocalDate.now().minusDays(1));

        claimService.updateClaim(lastYear, ClaimStatus.APPROVED);
        claimService.updateClaim(lastYearToo, ClaimStatus.APPROVED);

        assertThat(claimService.updateClaim(thisYear, ClaimStatus.APPROVED).getStatus())
                .isEqualTo(ClaimStatus.APPROVED);
    }

    private BigDecimal approvedTotal(TestPolicy policy) {
        return jdbcTemplate.queryForObject(
                "select coalesce(sum(amount), 0) from claims where policy_id = ? and status = 'APPROVED'",
                BigDecimal.class, policy.id());
    }

    private Long claimInReview(TestPolicy policy, String amount) {
        return claimInReview(policy, amount, LocalDate.now().minusDays(1));
    }

    private Long claimInReview(TestPolicy policy, String amount, LocalDate lossDate) {
        Long id = claimService.createClaimResponse(new CreateClaimRequest(policy.id(), lossDate,
                policy.customerNumber(), ClaimType.LIABILITY, "Haftpflichtschaden", new BigDecimal(amount))).id();
        claimService.updateClaim(id, ClaimStatus.IN_REVIEW);
        return id;
    }

    private TestPolicy createLiabilityPolicy() {
        return createLiabilityPolicy(LocalDate.now().minusDays(30));
    }

    private TestPolicy createLiabilityPolicy(LocalDate validFrom) {
        String suffix = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "insert into customers (customer_number, display_name, email, active) values (?, ?, ?, true)",
                "AGG-" + suffix, "Aggregate Test", suffix + "@example.test");
        Long customerId = jdbcTemplate.queryForObject(
                "select id from customers where customer_number = ?", Long.class, "AGG-" + suffix);
        jdbcTemplate.update(
                "insert into products (code, name, coverage_limit, premium, active, annual_aggregate_factor) "
                        + "values (?, ?, ?, ?, true, null)",
                "AGG-" + suffix, "Haftpflicht Test", COVERAGE_LIMIT, new BigDecimal("68.00"));
        Long productId = jdbcTemplate.queryForObject(
                "select id from products where code = ?", Long.class, "AGG-" + suffix);
        Policy policy = policyRepository.save(new Policy(customerId, productId, validFrom,
                LocalDate.now().plusYears(1), COVERAGE_LIMIT, new BigDecimal("68.00"), ANNUAL_AGGREGATE));
        return new TestPolicy(policy.getId(), "AGG-" + suffix);
    }

    private record TestPolicy(Long id, String customerNumber) {
    }
}
