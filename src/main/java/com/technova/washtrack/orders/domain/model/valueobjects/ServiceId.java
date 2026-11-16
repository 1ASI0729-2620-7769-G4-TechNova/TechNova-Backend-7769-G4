package com.technova.washtrack.orders.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/**
 * Value object representing the identifier of a laundry service (washing, dry cleaning, ironing, etc.)
 * configured in the Customer & Laundry Management bounded context.
 * Order Management only keeps this reference and never accesses the external aggregate directly.
 *
 * @param value raw service identifier
 */
@Embeddable
public record ServiceId(Long value) {
    private static final String INVALID_MESSAGE_KEY = "order.error.serviceId.invalid";

    /**
     * Validates the service identifier.
     *
     * @throws IllegalArgumentException if the value is null or not greater than 0
     */
    public ServiceId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }
}