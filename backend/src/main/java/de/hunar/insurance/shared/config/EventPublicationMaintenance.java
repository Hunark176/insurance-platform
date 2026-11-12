package de.hunar.insurance.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.CompletedEventPublications;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;

/**
 * Keeps the persistent event publication registry (outbox) healthy at runtime.
 *
 * <p>Events such as {@code ClaimApprovedEvent} are stored together with the business
 * transaction. If a listener fails, its publication stays incomplete. Spring Modulith
 * re-delivers incomplete publications on startup; this component additionally retries them
 * periodically while the application is running, so a temporary failure (for example a
 * database hiccup) does not leave a claim without payout until the next restart.</p>
 *
 * <p>Retrying is safe because the billing listeners are idempotent and backed by unique
 * constraints ({@code payouts.claim_id}, {@code invoices.policy_id}).</p>
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "insurance.events.maintenance", name = "enabled", havingValue = "true",
        matchIfMissing = true)
public class EventPublicationMaintenance {

    private final IncompleteEventPublications incompletePublications;
    private final CompletedEventPublications completedPublications;

    /** Minimum age before an incomplete publication is retried, so in-flight work is not duplicated. */
    @Value("${insurance.events.maintenance.retry-older-than:PT5M}")
    private Duration retryOlderThan;

    /** How long completed publications are kept for troubleshooting before they are removed. */
    @Value("${insurance.events.maintenance.completed-retention:P7D}")
    private Duration completedRetention;

    /**
     * Re-delivers publications whose listener has not completed within {@link #retryOlderThan}.
     */
    @Scheduled(fixedDelayString = "${insurance.events.maintenance.retry-interval:PT1M}",
            initialDelayString = "${insurance.events.maintenance.retry-interval:PT1M}")
    void retryIncompletePublications() {
        try {
            incompletePublications.resubmitIncompletePublicationsOlderThan(retryOlderThan);
        } catch (RuntimeException exception) {
            // Never let a failing retry stop the scheduler; the next run tries again.
            log.warn("Retrying incomplete event publications failed: {}", exception.getMessage());
        }
    }

    /**
     * Removes completed publications older than {@link #completedRetention} so the table stays small.
     */
    @Scheduled(cron = "${insurance.events.maintenance.cleanup-cron:0 30 3 * * *}")
    void deleteOldCompletedPublications() {
        completedPublications.deletePublicationsOlderThan(completedRetention);
        log.debug("Deleted completed event publications older than {}", completedRetention);
    }
}
