package de.hunar.insurance.billing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs exactly the SQL of {@code ops/reset-demo-data.sh} against PostgreSQL: the reset restores
 * the original demo data, and a failure during the reset leaves the existing data untouched.
 * Skipped only locally without Docker, never in CI.
 */
@DisabledIf("de.hunar.insurance.support.DockerAvailability#skipLocallyWithoutDocker")
@Testcontainers
@SpringBootTest
@ActiveProfiles("prod")
class DemoDataResetIntegrationTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine");

    private static final FileSystemResource RESET_SQL = new FileSystemResource("../ops/reset-demo-data.sql");
    private static final FileSystemResource DEMO_DATA = new FileSystemResource("src/main/resources/db/dev-data.sql");

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void resetRestoresTheDemoDataAndRemovesVisitorChanges() {
        jdbcTemplate.update("insert into customers (customer_number, display_name, email, active) "
                + "values ('VISITOR-1', 'Besucher', 'visitor@example.test', true)");

        runInOneTransaction(new ResourceDatabasePopulator(RESET_SQL, DEMO_DATA));

        assertThat(count("customers")).isEqualTo(50);
        assertThat(count("policies")).isEqualTo(65);
        assertThat(count("claims")).isPositive();
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from customers where customer_number = 'VISITOR-1'", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from policies p join products pr on pr.id = p.product_id "
                        + "where pr.code = 'PRIVAT-HAFTPFLICHT' and p.annual_aggregate_limit is null", Integer.class))
                .as("annual aggregate of liability policies is restored")
                .isZero();
    }

    @Test
    void failedResetLeavesTheExistingDataUntouched() {
        int customersBefore = count("customers");
        ByteArrayResource brokenDemoData = new ByteArrayResource(
                "insert into customers (no_such_column) values (1);".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> runInOneTransaction(new ResourceDatabasePopulator(RESET_SQL, brokenDemoData)))
                .isInstanceOf(RuntimeException.class);

        // The TRUNCATE was rolled back together with the failed insert: the site is never empty.
        assertThat(count("customers")).isEqualTo(customersBefore).isPositive();
    }

    /** Same guarantee as {@code psql --single-transaction}: all statements commit or none. */
    private void runInOneTransaction(ResourceDatabasePopulator populator) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> populator.execute(dataSource));
    }

    private int count(String table) {
        return jdbcTemplate.queryForObject("select count(*) from " + table, Integer.class);
    }
}
