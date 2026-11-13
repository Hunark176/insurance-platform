package de.hunar.insurance.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables {@code @Scheduled} jobs (outbox maintenance, idempotency cleanup) independently of
 * whether a single job is switched off by configuration.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
