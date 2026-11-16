package com.technova.washtrack.orders.domain.model.valueobjects;

/**
 * Value object representing the identifier of a customer registered in the Customer & Laundry Management bounded context.
 * Order Management only keeps this reference and never accesses the external aggregate directly.
 *
 * @param value raw customer identifier
 */
public record CustomerId(Long value) {
    private static final String INVALID_MESSAGE_KEY = "order.error.customerId.invalid";

    /**
     * Validates the customer identifier.
     *
     * @throws IllegalArgumentException if the value is null or not greater than 0
     */
    public CustomerId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }
}