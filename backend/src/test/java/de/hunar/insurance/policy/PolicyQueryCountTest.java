package de.hunar.insurance.policy;

import de.hunar.insurance.policy.application.PolicyService;
import de.hunar.insurance.policy.dto.PolicyResponse;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards against N+1 lookups across module boundaries: listing policies must use a constant
 * number of SQL statements, independent of how many policies, customers and products exist.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "insurance.dev.h2.tcp.enabled=false",
        "insurance.events.maintenance.enabled=false",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
class PolicyQueryCountTest {

    /** Page and count query for the policies, one batch query each for customers and products. */
    private static final long MAX_STATEMENTS = 4;

    @Autowired
    private PolicyService policyService;
    @Autowired
    private PolicyApi policyApi;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;

    @BeforeEach
    void resetStatistics() {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
    }

    @Test
    void listingPoliciesUsesAConstantNumberOfQueries() {
        List<PolicyResponse> policies = policyService.findPage(PageRequest.of(0, 100)).content();

        // The demo data must contain several customers and products, otherwise the test proves nothing.
        assertThat(policies.stream().map(PolicyResponse::customerId).distinct().count()).isGreaterThan(1);
        assertThat(policies.stream().map(PolicyResponse::productId).distinct().count()).isGreaterThan(1);
        assertThat(policies).allSatisfy(policy -> {
            assertThat(policy.customerName()).isNotBlank();
            assertThat(policy.productName()).isNotBlank();
        });
        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(MAX_STATEMENTS);
    }

    @Test
    void loadingSnapshotsForSeveralPoliciesUsesAConstantNumberOfQueries() {
        List<Long> ids = policyService.findPage(PageRequest.of(0, 100)).content().stream()
                .map(PolicyResponse::id).toList();
        statistics.clear();

        List<PolicySnapshot> snapshots = policyApi.getSnapshots(ids);

        assertThat(snapshots).hasSameSizeAs(ids);
        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(2);
    }
}
