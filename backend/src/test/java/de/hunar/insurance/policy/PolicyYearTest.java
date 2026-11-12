package de.hunar.insurance.policy;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyYearTest {

    @Test
    void policyYearsStartOnTheAnniversaryOfThePolicyStart() {
        LocalDate start = LocalDate.of(2025, 4, 1);

        assertThat(PolicyYear.containing(start, LocalDate.of(2025, 4, 1)))
                .isEqualTo(new PolicyYear(LocalDate.of(2025, 4, 1), LocalDate.of(2026, 3, 31)));
        assertThat(PolicyYear.containing(start, LocalDate.of(2026, 3, 31)))
                .isEqualTo(new PolicyYear(LocalDate.of(2025, 4, 1), LocalDate.of(2026, 3, 31)));
        assertThat(PolicyYear.containing(start, LocalDate.of(2026, 4, 1)))
                .isEqualTo(new PolicyYear(LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31)));
    }

    @Test
    void leapDayStartProducesContiguousYearsWithoutGaps() {
        LocalDate start = LocalDate.of(2024, 2, 29);

        PolicyYear first = PolicyYear.containing(start, LocalDate.of(2025, 2, 27));
        PolicyYear second = PolicyYear.containing(start, LocalDate.of(2025, 2, 28));

        assertThat(first).isEqualTo(new PolicyYear(LocalDate.of(2024, 2, 29), LocalDate.of(2025, 2, 27)));
        assertThat(second.start()).isEqualTo(first.end().plusDays(1));
        assertThat(second.contains(LocalDate.of(2025, 2, 28))).isTrue();
    }

    @Test
    void everyDayBelongsToExactlyOnePolicyYear() {
        LocalDate start = LocalDate.of(2024, 2, 29);
        for (LocalDate day = start; day.isBefore(LocalDate.of(2029, 1, 1)); day = day.plusDays(1)) {
            PolicyYear year = PolicyYear.containing(start, day);
            assertThat(year.contains(day)).as("year of %s", day).isTrue();
            assertThat(PolicyYear.containing(start, year.end().plusDays(1)).start())
                    .as("year after %s", year).isEqualTo(year.end().plusDays(1));
        }
    }

    @Test
    void datesBeforeThePolicyStartHaveNoPolicyYear() {
        assertThatThrownBy(() -> PolicyYear.containing(LocalDate.of(2026, 1, 1), LocalDate.of(2025, 12, 31)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
