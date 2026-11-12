package de.hunar.insurance.support;

import org.testcontainers.DockerClientFactory;

/**
 * Decides whether Docker-based integration tests may be skipped.
 *
 * <p>Locally, the tests are skipped when no Docker daemon is running, so the default suite
 * works without Docker. In CI ({@code CI} environment variable set, as on GitHub Actions)
 * they are never skipped: a broken Docker setup must fail the build instead of silently
 * hiding the PostgreSQL migration checks.</p>
 *
 * <p>Usage: {@code @DisabledIf("de.hunar.insurance.support.DockerAvailability#skipLocallyWithoutDocker")}</p>
 */
public final class DockerAvailability {

    private DockerAvailability() {
    }

    public static boolean skipLocallyWithoutDocker() {
        boolean runningInCi = System.getenv("CI") != null;
        return !runningInCi && !DockerClientFactory.instance().isDockerAvailable();
    }
}
