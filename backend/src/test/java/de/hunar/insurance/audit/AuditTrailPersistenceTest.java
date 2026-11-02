package de.hunar.insurance.audit;

import de.hunar.insurance.audit.application.AuditTrailService;
import de.hunar.insurance.audit.domain.AuditEntryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.sql.init.mode=never"
})
@ActiveProfiles("audit-test")
@Import({AuditTrailService.class, AuditTrailPersistenceTest.ClockConfiguration.class})
class AuditTrailPersistenceTest {
    @Autowired
    private AuditTrailService auditTrail;
    @Autowired
    private AuditEntryRepository repository;

    @AfterEach
    void clearRequestContext() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void writesAuditEntryWithActorRequestIdAndUtcTimeInsideCurrentTransaction() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("clerk", "n/a", List.of()));
        MDC.put("requestId", "7c78c775-3988-4ed1-984e-cf8bf847e14a");

        auditTrail.record("STATUS_CHANGED", "CLAIM", "42",
                "from=IN_REVIEW,to=APPROVED", auditTrail.captureContext());
        repository.flush();

        var entry = repository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
                "CLAIM", "42", org.springframework.data.domain.PageRequest.of(0, 10))
                .getContent()
                .get(0);
        assertThat(entry.getOccurredAt()).isEqualTo(Instant.parse("2026-11-01T12:00:00Z"));
        assertThat(entry.getActor()).isEqualTo("clerk");
        assertThat(entry.getRequestId()).isEqualTo("7c78c775-3988-4ed1-984e-cf8bf847e14a");
        assertThat(entry.getSummary()).isEqualTo("from=IN_REVIEW,to=APPROVED");
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ClockConfiguration {
        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-11-01T12:00:00Z"), ZoneOffset.UTC);
        }
    }
}
