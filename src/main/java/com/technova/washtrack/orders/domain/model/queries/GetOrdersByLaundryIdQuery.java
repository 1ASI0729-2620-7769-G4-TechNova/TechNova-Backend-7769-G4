package com.technova.washtrack.orders.domain.model.queries;

import com.technova.washtrack.orders.domain.model.valueobjects.LaundryId;

/**
 * Query for retrieving all the orders received by a laundry.
 *
 * @param laundryId laundry identifier value object
 *
 */
public record GetOrdersByLaundryIdQuery(LaundryId laundryId) {
    /**
     * Validates the query.
     *
     * @throws IllegalArgumentException if {@code laundryId} is null
     */
    public GetOrdersByLaundryIdQuery {
        if (laundryId == null) {
            throw new IllegalArgumentException("order.error.laundryId.notNull");
        }
    }
}