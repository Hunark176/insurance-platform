package de.hunar.insurance.billing;

import de.hunar.insurance.audit.AuditContext;
import de.hunar.insurance.billing.domain.PayoutRepository;
import de.hunar.insurance.claim.events.ClaimApprovedEvent;
import de.hunar.insurance.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Verifies the transactional outbox: published events are persisted, completed by their
 * listener, and kept for re-delivery when a listener fails.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "insurance.dev.h2.tcp.enabled=false",
        // The scheduled retry is disabled so the test controls re-delivery explicitly.
        "insurance.events.maintenance.enabled=false"
})
@Import(EventOutboxIntegrationTest.FlakyListenerConfig.class)
class EventOutboxIntegrationTest {

    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private PayoutRepository payoutRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private IncompleteEventPublications incompletePublications;
    @Autowired
    private FlakyListener flakyListener;

    @BeforeEach
    void reset() {
        flakyListener.reset();
    }

    @Test
    void approvedClaimIsStoredInTheOutboxAndCompletedByTheBillingListener() {
        long claimId = 9_100_001L;

        publishInTransaction(approvalOf(claimId));

        // Listeners run asynchronously after the commit.
        await().untilAsserted(() -> {
            assertThat(payoutRepository.existsByClaimId(claimId)).isTrue();
            assertThat(publications("ClaimApprovedListener", claimId, true)).isEqualTo(1);
        });
        assertThat(publications("ClaimApprovedListener", claimId, false)).isZero();
    }

    @Test
    void failedListenerStaysIncompleteAndIsDeliveredOnRetry() {
        long claimId = 9_100_002L;
        flakyListener.setFailing(true);

        publishInTransaction(approvalOf(claimId));

        // Wait until the first delivery has actually failed, so it cannot race the retry.
        await().untilAsserted(() -> assertThat(flakyListener.attempts()).isEqualTo(1));
        // The billing listener still works independently of the failing one.
        await().untilAsserted(() -> assertThat(payoutRepository.existsByClaimId(claimId)).isTrue());

        // The outage did not lose the event: it is stored as an incomplete publication.
        assertThat(flakyListener.handledClaimIds()).isEmpty();
        assertThat(publications("FlakyListener", claimId, false)).isEqualTo(1);

        // After recovery the retry deserializes the stored event and delivers it.
        flakyListener.setFailing(false);
        incompletePublications.resubmitIncompletePublications(publication -> true);

        await().untilAsserted(() -> {
            assertThat(flakyListener.handledClaimIds()).containsExactly(claimId);
            assertThat(publications("FlakyListener", claimId, true)).isEqualTo(1);
        });
        assertThat(publications("FlakyListener", claimId, false)).isZero();
    }

    private static ClaimApprovedEvent approvalOf(long claimId) {
        return new ClaimApprovedEvent(claimId, 1L, Money.eur(new BigDecimal("123.45")),
                new AuditContext("clerk", "test-request"));
    }

    private void publishInTransaction(Object event) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> publisher.publishEvent(event));
    }

    private int publications(String listener, long claimId, boolean completed) {
        return jdbcTemplate.queryForObject("""
                        select count(*) from event_publication
                        where listener_id like ? and serialized_event like ?
                          and completion_date is %s null
                        """.formatted(completed ? "not" : ""),
                Integer.class, "%" + listener + "%", "%\"claimId\":" + claimId + "%");
    }

    /**
     * Listener that simulates a temporary outage of a downstream module.
     *
     * <p>State is accessed through methods because the transactional listener is proxied;
     * fields read through the proxy would not reflect the target's state.</p>
     */
    static class FlakyListener {
        private final AtomicBoolean failing = new AtomicBoolean();
        private final AtomicInteger attempts = new AtomicInteger();
        private final List<Long> handledClaimIds = new CopyOnWriteArrayList<>();

        @ApplicationModuleListener
        void on(ClaimApprovedEvent event) {
            attempts.incrementAndGet();
            if (failing.get()) {
                throw new IllegalStateException("simulated outage");
            }
            handledClaimIds.add(event.claimId());
        }

        public void setFailing(boolean value) {
            failing.set(value);
        }

        public List<Long> handledClaimIds() {
            return List.copyOf(handledClaimIds);
        }

        public int attempts() {
            return attempts.get();
        }

        public void reset() {
            failing.set(false);
            attempts.set(0);
            handledClaimIds.clear();
        }
    }

    @TestConfiguration
    static class FlakyListenerConfig {
        @Bean
        FlakyListener flakyListener() {
            return new FlakyListener();
        }
    }
}
