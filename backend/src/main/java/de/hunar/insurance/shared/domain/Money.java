package de.hunar.insurance.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Immutable monetary value with an explicit currency and two-decimal scale.
 *
 * <p>Amounts are rounded using {@link RoundingMode#HALF_UP}; negative values are not accepted.</p>
 *
 * @param amount non-negative amount
 * @param currency currency associated with the amount
 */
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Creates a euro-denominated amount.
     *
     * @param amount non-negative amount to represent in EUR
     * @return the normalized monetary value
     * @throws NullPointerException if {@code amount} is {@code null}
     * @throws IllegalArgumentException if {@code amount} is negative
     */
    public static Money eur(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("EUR"));
    }
}
