package de.hunar.insurance.billing;

import de.hunar.insurance.billing.domain.CreditNote;
import de.hunar.insurance.billing.domain.CreditNoteRepository;
import de.hunar.insurance.policy.application.PolicyService;
import de.hunar.insurance.policy.domain.Policy;
import de.hunar.insurance.policy.domain.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Cancelling a policy creates exactly one pro-rata credit note through the outbox event.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "insurance.dev.h2.tcp.enabled=false",
        "insurance.events.maintenance.enabled=false"
})
class PremiumRefundIntegrationTest {
    @Autowired
    private PolicyService policyService;
    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private CreditNoteRepository creditNotes;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void cancellationCreatesOneProRataCreditNote() {
        LocalDate validFrom = LocalDate.now().minusDays(99);
        LocalDate validTo = validFrom.plusDays(199);   // 200-day term
        Long policyId = createPolicy(validFrom, validTo, "200.00");

        policyService.cancel(policyId, null);   // effective today: day 100 of 200

        CreditNote note = await().until(() -> creditNotes.findByPolicyId(policyId), java.util.Optional::isPresent)
                .orElseThrow();
        long expectedDays = ChronoUnit.DAYS.between(LocalDate.now(), validTo);
        assertThat(note.getTermDays()).isEqualTo(200);
        assertThat(note.getCreditedDays()).isEqualTo(expectedDays);
        assertThat(note.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(expectedDays));   // 1.00 per day
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from credit_notes where policy_id = ?", Integer.class, policyId)).isEqualTo(1);
    }

    private Long createPolicy(LocalDate validFrom, LocalDate validTo, String premium) {
        String suffix = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "insert into customers (customer_number, display_name, email, active) values (?, ?, ?, true)",
                "REF-" + suffix, "Refund Test", suffix + "@example.test");
        Long customerId = jdbcTemplate.queryForObject(
                "select id from customers where customer_number = ?", Long.class, "REF-" + suffix);
        jdbcTemplate.update(
                "insert into products (code, name, coverage_limit, premium, active) values (?, ?, ?, ?, true)",
                "REF-" + suffix, "Refund Product", new BigDecimal("10000.00"), new BigDecimal(premium));
        Long productId = jdbcTemplate.queryForObject(
                "select id from products where code = ?", Long.class, "REF-" + suffix);
        return policyRepository.save(new Policy(customerId, productId, validFrom, validTo,
                new BigDecimal("10000.00"), new BigDecimal(premium))).getId();
    }
}
