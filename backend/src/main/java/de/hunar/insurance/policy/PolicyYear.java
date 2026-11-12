package de.hunar.insurance.policy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * One policy year: the period between two anniversaries of the policy start, used for annual
 * aggregate limits.
 *
 * <p>Year {@code k} runs from {@code start.plusYears(k)} to the day before
 * {@code start.plusYears(k + 1)}. The years are contiguous and do not overlap; for a start on
 * 29 February the anniversary falls back to 28 February in non-leap years.</p>
 *
 * @param start first day of the policy year
 * @param end last day of the policy year (inclusive)
 */
public record PolicyYear(LocalDate start, LocalDate end) {

    public PolicyYear {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end must not be before start");
        }
    }

    /**
     * Determines the policy year that contains {@code date}.
     *
     * @param policyStart first day of the policy term
     * @param date date to locate
     * @return the containing policy year
     * @throws IllegalArgumentException if {@code date} lies before {@code policyStart}
     */
    public static PolicyYear containing(LocalDate policyStart, LocalDate date) {
        Objects.requireNonNull(policyStart, "policyStart must not be null");
        Objects.requireNonNull(date, "date must not be null");
        if (date.isBefore(policyStart)) {
            throw new IllegalArgumentException("date must not be before the policy start");
        }
        long year = ChronoUnit.YEARS.between(policyStart, date);
        // YEARS.between can be one short around 29 February; move forward until the date fits.
        while (!date.isBefore(policyStart.plusYears(year + 1))) {
            year++;
        }
        return new PolicyYear(policyStart.plusYears(year), policyStart.plusYears(year + 1).minusDays(1));
    }

    /**
     * @param date date to check
     * @return {@code true} if the date lies within this policy year, both ends inclusive
     */
    public boolean contains(LocalDate date) {
        return !date.isBefore(start) && !date.isAfter(end);
    }
}
