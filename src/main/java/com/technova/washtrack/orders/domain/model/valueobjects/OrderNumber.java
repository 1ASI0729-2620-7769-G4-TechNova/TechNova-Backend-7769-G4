package com.technova.washtrack.orders.domain.model.valueobjects;

import java.util.regex.Pattern;

/**
 * Value object representing the unique business number of an order (for example {@code ORD-0001}).
 *
 * @param value raw order number value
 */
public record OrderNumber(String value) {
    private static final Pattern ALLOWED_PATTERN = Pattern.compile("^ORD-\\d{4,}$");
    private static final String NOT_BLANK_MESSAGE_KEY = "order.error.orderNumber.notBlank";
    private static final String PATTERN_MESSAGE_KEY = "order.error.orderNumber.pattern";

    /**
     * Validates the order number.
     *
     * @throws IllegalArgumentException if the value is null, blank or does not match the {@code ORD-0001} format
     */
    public OrderNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(NOT_BLANK_MESSAGE_KEY);
        }
        if (!ALLOWED_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(PATTERN_MESSAGE_KEY);
        }
    }
}