package de.hunar.insurance.support;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Reusable opt-in fixture for repository integration tests.
 * It is deliberately not used by the default suite so local tests do not require Docker.
 */
@Testcontainers(disabledWithoutDocker = true)
public abstract class PostgresContainerSupport {
    @Container
    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("insurance")
                    .withUsername("insurance")
                    .withPassword("insurance");
}
