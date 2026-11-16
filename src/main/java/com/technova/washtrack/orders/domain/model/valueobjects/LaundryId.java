package com.technova.washtrack.orders.domain.model.valueobjects;

/**
 * Value object representing the identifier of a laundry registered in the Customer & Laundry Management bounded context.
 * Order Management only keeps this reference and never accesses the external aggregate directly.
 *
 * @param value raw laundry identifier
 */
public record LaundryId(Long value) {
    private static final String INVALID_MESSAGE_KEY = "order.error.laundryId.invalid";

    /**
     * Validates the laundry identifier.
     *
     * @throws IllegalArgumentException if the value is null or not greater than 0
     */
    public LaundryId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }
}