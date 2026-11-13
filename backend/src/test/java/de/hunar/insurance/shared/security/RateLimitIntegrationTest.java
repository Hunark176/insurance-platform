package de.hunar.insurance.shared.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The limiter runs in the real filter chain before Spring Security, so failed logins count too.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "insurance.dev.h2.tcp.enabled=false",
        "insurance.events.maintenance.enabled=false",
        "insurance.rate-limit.enabled=true",
        "insurance.rate-limit.writes-per-minute=2"
})
class RateLimitIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void failedLoginAttemptsAreLimitedBeforeAuthentication() throws Exception {
        for (int attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(patch("/api/claims/1/status").param("newStatus", "IN_REVIEW")
                            .with(httpBasic("clerk", "wrong-password")))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(patch("/api/claims/1/status").param("newStatus", "IN_REVIEW")
                        .with(httpBasic("clerk", "wrong-password")))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }
}
