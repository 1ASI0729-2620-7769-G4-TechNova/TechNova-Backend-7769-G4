package com.technova.washtrack.orders.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Value object representing a monetary amount in a specific currency.
 * <p>
 * Invariants:
 * - The amount is required and cannot be negative
 * - The currency is required and must be an ISO 4217 code of three letters (for example {@code PEN})
 * - The amount is always stored with two decimals, rounded half up
 * <p>
 * Every operation returns a new instance, so a Money value never changes after it is created.
 *
 * @param amount   monetary amount
 * @param currency ISO 4217 currency code
 * @since 1.0
 */
@Embeddable
public record Money(BigDecimal amount, String currency) {
    private static final int SCALE = 2;
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final String AMOUNT_NOT_NULL_MESSAGE_KEY = "order.error.money.amount.notNull";
    private static final String AMOUNT_NEGATIVE_MESSAGE_KEY = "order.error.money.amount.negative";
    private static final String CURRENCY_NOT_BLANK_MESSAGE_KEY = "order.error.money.currency.notBlank";
    private static final String CURRENCY_PATTERN_MESSAGE_KEY = "order.error.money.currency.pattern";
    private static final String CURRENCY_MISMATCH_MESSAGE_KEY = "order.error.money.currency.mismatch";
    private static final String FACTOR_INVALID_MESSAGE_KEY = "order.error.money.factor.invalid";

    /**
     * Validates and normalizes the monetary amount and currency.
     *
     * @throws IllegalArgumentException if the amount is null or negative,
     *                                  or if the currency is blank or not a three-letter code
     */
    public Money {
        if (amount == null) {
            throw new IllegalArgumentException(AMOUNT_NOT_NULL_MESSAGE_KEY);
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException(AMOUNT_NEGATIVE_MESSAGE_KEY);
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException(CURRENCY_NOT_BLANK_MESSAGE_KEY);
        }
        currency = currency.trim().toUpperCase(Locale.ROOT);
        if (!CURRENCY_PATTERN.matcher(currency).matches()) {
            throw new IllegalArgumentException(CURRENCY_PATTERN_MESSAGE_KEY);
        }
        amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Adds another monetary amount to this one.
     *
     * @param other monetary amount to add; it must use the same currency
     * @return new Money with the sum of both amounts
     * @throws IllegalArgumentException if the other amount is null or uses a different currency
     */
    public Money add(Money other) {
        if (other == null || !currency.equals(other.currency())) {
            throw new IllegalArgumentException(CURRENCY_MISMATCH_MESSAGE_KEY);
        }
        return new Money(amount.add(other.amount()), currency);
    }

    /**
     * Multiplies this monetary amount by a factor (for example, the quantity of garments).
     *
     * @param factor multiplication factor; it cannot be null or negative
     * @return new Money with the multiplied amount, rounded to two decimals
     * @throws IllegalArgumentException if the factor is null or negative
     */
    public Money multiply(BigDecimal factor) {
        if (factor == null || factor.signum() < 0) {
            throw new IllegalArgumentException(FACTOR_INVALID_MESSAGE_KEY);
        }
        return new Money(amount.multiply(factor), currency);
    }
}