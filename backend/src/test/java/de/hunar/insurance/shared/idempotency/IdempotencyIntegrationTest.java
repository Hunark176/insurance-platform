package de.hunar.insurance.shared.idempotency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hunar.insurance.policy.domain.Policy;
import de.hunar.insurance.policy.domain.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end behaviour of the Idempotency-Key header on {@code POST /api/claims}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "insurance.dev.h2.tcp.enabled=false",
        "insurance.events.maintenance.enabled=false",
        "insurance.idempotency.enabled=true"
})
class IdempotencyIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PolicyRepository policyRepository;

    private Long policyId;
    private String customerNumber;

    @BeforeEach
    void createPolicy() {
        String suffix = UUID.randomUUID().toString();
        customerNumber = "IDEM-" + suffix;
        jdbcTemplate.update(
                "insert into customers (customer_number, display_name, email, active) values (?, ?, ?, true)",
                customerNumber, "Idempotency Test", suffix + "@example.test");
        Long customerId = jdbcTemplate.queryForObject(
                "select id from customers where customer_number = ?", Long.class, customerNumber);
        jdbcTemplate.update(
                "insert into products (code, name, coverage_limit, premium, active) values (?, ?, ?, ?, true)",
                "IDEM-" + suffix, "Test Product", new BigDecimal("100000.00"), new BigDecimal("10.00"));
        Long productId = jdbcTemplate.queryForObject(
                "select id from products where code = ?", Long.class, "IDEM-" + suffix);
        policyId = policyRepository.save(new Policy(customerId, productId, LocalDate.now().minusDays(10),
                LocalDate.now().plusYears(1), new BigDecimal("100000.00"), new BigDecimal("10.00"))).getId();
    }

    @Test
    void retryWithTheSameKeyReturnsTheFirstResponseWithoutCreatingASecondClaim() throws Exception {
        String key = UUID.randomUUID().toString();

        MvcResult first = mockMvc.perform(reportClaim(key, "100.00", "clerk"))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist(IdempotencyFilter.REPLAYED_HEADER))
                .andReturn();
        MvcResult retry = mockMvc.perform(reportClaim(key, "100.00", "clerk"))
                .andExpect(status().isCreated())
                .andExpect(header().string(IdempotencyFilter.REPLAYED_HEADER, "true"))
                .andReturn();

        assertThat(claimId(retry)).isEqualTo(claimId(first));
        assertThat(claimsOfPolicy()).isEqualTo(1);
    }

    @Test
    void reusingAKeyForADifferentRequestIsRejected() throws Exception {
        String key = UUID.randomUUID().toString();
        mockMvc.perform(reportClaim(key, "100.00", "clerk")).andExpect(status().isCreated());

        mockMvc.perform(reportClaim(key, "999.00", "clerk"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Idempotency-Key was already used for a different request"));
        assertThat(claimsOfPolicy()).isEqualTo(1);
    }

    @Test
    void keysAreScopedPerUser() throws Exception {
        String key = UUID.randomUUID().toString();
        mockMvc.perform(reportClaim(key, "100.00", "clerk")).andExpect(status().isCreated());

        mockMvc.perform(reportClaim(key, "100.00", "admin"))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist(IdempotencyFilter.REPLAYED_HEADER));
        assertThat(claimsOfPolicy()).isEqualTo(2);
    }

    @Test
    void requestsWithoutKeyAreNotAffected() throws Exception {
        mockMvc.perform(reportClaim(null, "100.00", "clerk")).andExpect(status().isCreated());
        mockMvc.perform(reportClaim(null, "100.00", "clerk")).andExpect(status().isCreated());

        assertThat(claimsOfPolicy()).isEqualTo(2);
    }

    @Test
    void invalidKeysAreRejected() throws Exception {
        mockMvc.perform(reportClaim("short", "100.00", "clerk"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        assertThat(claimsOfPolicy()).isZero();
    }

    @Test
    void clientErrorsAreStoredAndReplayedWithoutSideEffects() throws Exception {
        String key = UUID.randomUUID().toString();
        // A customer may not report a claim for a foreign policy. Like every non-5xx outcome the
        // 403 is stored, so a retry gets the same answer; no claim is created either time.
        mockMvc.perform(reportClaim(key, "100.00", "customer")).andExpect(status().isForbidden());
        mockMvc.perform(reportClaim(key, "100.00", "customer"))
                .andExpect(status().isForbidden())
                .andExpect(header().string(IdempotencyFilter.REPLAYED_HEADER, "true"));
        assertThat(claimsOfPolicy()).isZero();
    }

    @Test
    void concurrentRequestsWithTheSameKeyCreateOnlyOneClaim() throws Exception {
        String key = UUID.randomUUID().toString();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Integer>> results = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                results.add(executor.submit(() -> {
                    start.await();
                    return mockMvc.perform(reportClaim(key, "100.00", "clerk")).andReturn().getResponse().getStatus();
                }));
            }
            start.countDown();
            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> result : results) {
                statuses.add(result.get(30, TimeUnit.SECONDS));
            }

            // Each request either created/replayed the claim (201) or saw the first one in progress (409).
            assertThat(statuses).allMatch(code -> code == 201 || code == 409).contains(201);
            assertThat(claimsOfPolicy()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private MockHttpServletRequestBuilder reportClaim(String key, String amount, String user) {
        String body = """
                {"policyId": %d, "occurredOn": "%s", "customerNumber": "%s",
                 "claimType": "AUTO", "description": "Idempotency test", "amount": %s}
                """.formatted(policyId, LocalDate.now().minusDays(1), customerNumber, amount);
        MockHttpServletRequestBuilder request = post("/api/claims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(httpBasic(user, user));
        return key == null ? request : request.header(IdempotencyFilter.HEADER, key);
    }

    private long claimId(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private int claimsOfPolicy() {
        return jdbcTemplate.queryForObject("select count(*) from claims where policy_id = ?", Integer.class, policyId);
    }
}
