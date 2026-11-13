package de.hunar.insurance.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Reports event publications that have stayed incomplete for too long.
 *
 * <p>A listener that keeps failing is retried by {@link EventPublicationMaintenance}, but nobody
 * would notice the growing backlog in the log. This indicator turns {@code /actuator/health}
 * {@code DOWN} and logs an error while publications older than {@code stuck-after} are
 * incomplete. It is deliberately <em>not</em> part of the readiness group: a stuck billing
 * listener must not take the whole application out of service.</p>
 */
@Slf4j
@Component("eventPublications")
public class EventPublicationHealthIndicator implements HealthIndicator {
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;
    private final Duration stuckAfter;

    public EventPublicationHealthIndicator(JdbcTemplate jdbcTemplate, Clock clock,
                                           @Value("${insurance.events.maintenance.stuck-after:PT30M}")
                                           Duration stuckAfter) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
        this.stuckAfter = stuckAfter;
    }

    @Override
    public Health health() {
        Instant threshold = clock.instant().minus(stuckAfter);
        Integer stuck = jdbcTemplate.queryForObject(
                "select count(*) from event_publication where completion_date is null and publication_date < ?",
                Integer.class, Timestamp.from(threshold));
        int count = stuck == null ? 0 : stuck;
        if (count > 0) {
            log.error("Event publications incomplete for more than {}: count={}", stuckAfter, count);
            return Health.down().withDetail("stuckPublications", count).withDetail("stuckAfter", stuckAfter.toString())
                    .build();
        }
        return Health.up().build();
    }
}
