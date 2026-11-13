package de.hunar.insurance.shared.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A publication that stays incomplete too long turns the overall health DOWN, but readiness
 * (used by the Docker healthcheck) stays UP so the application keeps serving requests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "insurance.dev.h2.tcp.enabled=false",
        "insurance.events.maintenance.enabled=false"
})
class EventPublicationHealthIndicatorTest {
    private final UUID stuckId = UUID.randomUUID();

    @Autowired
    private EventPublicationHealthIndicator indicator;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void removeTestPublication() {
        jdbcTemplate.update("delete from event_publication where id = ?", stuckId);
    }

    @Test
    void isUpWithoutStuckPublications() {
        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void reportsPublicationsIncompleteForTooLongWithoutAffectingReadiness() throws Exception {
        insertPublication(Instant.now().minus(Duration.ofHours(2)));

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
        assertThat(indicator.health().getDetails()).containsEntry("stuckPublications", 1);
        mockMvc.perform(get("/actuator/health")).andExpect(status().isServiceUnavailable());
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void recentIncompletePublicationsAreStillConsideredInFlight() {
        insertPublication(Instant.now().minus(Duration.ofMinutes(5)));

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }

    private void insertPublication(Instant publishedAt) {
        jdbcTemplate.update("""
                        insert into event_publication (id, listener_id, event_type, serialized_event, publication_date)
                        values (?, 'test.Listener.on(java.lang.Object)', 'java.lang.Object', '{}', ?)
                        """, stuckId, Timestamp.from(publishedAt));
    }
}
