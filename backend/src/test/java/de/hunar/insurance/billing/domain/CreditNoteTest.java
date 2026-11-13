package de.hunar.insurance.billing.domain;

import de.hunar.insurance.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CreditNoteTest {
    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 12, 31);   // 365 days
    private static final Instant NOW = Instant.parse("2026-06-30T10:00:00Z");

    @Test
    void creditsTheDaysAfterTheCancellationProRata() {
        // Covered 1 Jan – 30 Jun (181 days), credited 1 Jul – 31 Dec (184 days).
        CreditNote note = credit("365.00", LocalDate.of(2026, 6, 30)).orElseThrow();

        assertThat(note.getCreditedDays()).isEqualTo(184);
        assertThat(note.getTermDays()).isEqualTo(365);
        assertThat(note.getAmount()).isEqualByComparingTo("184.00");
        assertThat(note.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void roundsHalfUpToCents() {
        // 100.00 × 1 / 365 = 0.27397… → 0.27; 100.00 × 2 / 365 = 0.54794… → 0.55
        assertThat(credit("100.00", END.minusDays(1)).orElseThrow().getAmount()).isEqualByComparingTo("0.27");
        assertThat(credit("100.00", END.minusDays(2)).orElseThrow().getAmount()).isEqualByComparingTo("0.55");
    }

    @Test
    void cancellationOnTheLastDayCreditsNothing() {
        assertThat(credit("365.00", END)).isEmpty();
    }

    @Test
    void cancellationBeforeTheTermStartsCreditsTheFullPremium() {
        CreditNote note = credit("365.00", START.minusDays(10)).orElseThrow();

        assertThat(note.getCreditedDays()).isEqualTo(365);
        assertThat(note.getAmount()).isEqualByComparingTo("365.00");
    }

    @Test
    void cancellationOnTheFirstDayKeepsOneDay() {
        assertThat(credit("365.00", START).orElseThrow().getAmount()).isEqualByComparingTo("364.00");
    }

    @Test
    void freePoliciesNeedNoCreditNote() {
        assertThat(credit("0.00", LocalDate.of(2026, 6, 30))).isEmpty();
    }

    private static Optional<CreditNote> credit(String premium, LocalDate cancelledOn) {
        return CreditNote.forCancellation(1L, 2L, Money.eur(new BigDecimal(premium)), START, END, cancelledOn, NOW);
    }
}
