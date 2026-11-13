FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY backend/pom.xml backend/mvnw backend/mvnw.cmd ./
COPY backend/.mvn ./.mvn
RUN ./mvnw dependency:go-offline -B
COPY backend/src ./src
# Tests run in CI before any image is built (see .github/workflows/deploy.yml).
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:21-jre

# curl is used by the healthcheck; install it only if the base image lacks it.
RUN command -v curl >/dev/null 2>&1 \
    || (apt-get update \
        && apt-get install -y --no-install-recommends curl \
        && rm -rf /var/lib/apt/lists/*)

# Run as an unprivileged system user instead of root. The application writes
# only to /tmp (Tomcat work directory) and, in the local/dev profiles, to
# /app/logs; production logs go to stdout.
RUN useradd --system --uid 10001 --no-create-home --shell /usr/sbin/nologin app \
    && mkdir -p /app/logs \
    && chown app /app/logs

WORKDIR /app
COPY --from=build --chown=root:root /workspace/target/insurance-platform-0.0.1-SNAPSHOT.jar app.jar
USER app

EXPOSE 8080

# Healthy = application started (Flyway ran) and the database answers.
# The start period leaves time for migrations on a fresh database.
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=4 \
    CMD curl -fsS http://localhost:8080/actuator/health/readiness >/dev/null || exit 1

# Size the heap relative to the container memory limit instead of the host.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
