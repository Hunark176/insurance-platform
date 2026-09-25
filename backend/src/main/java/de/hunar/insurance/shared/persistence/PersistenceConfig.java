package de.hunar.insurance.shared.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Shared persistence switchboard. Domain entities own their lifecycle timestamps;
 * auditing is enabled so future modules can add created/modified principals without
 * changing application bootstrap configuration.
 */
@Configuration
@EnableJpaAuditing
public class PersistenceConfig {
}
