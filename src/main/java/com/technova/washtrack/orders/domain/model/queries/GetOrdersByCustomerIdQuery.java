package com.technova.washtrack.orders.domain.model.queries;

import com.technova.washtrack.orders.domain.model.valueobjects.CustomerId;

/**
 * Query for retrieving all the orders of a customer (current and previous ones).
 *
 * @param customerId customer identifier value object
 *
 */
public record GetOrdersByCustomerIdQuery(CustomerId customerId) {
    /**
     * Validates the query.
     *
     * @throws IllegalArgumentException if {@code customerId} is null
     */
    public GetOrdersByCustomerIdQuery {
        if (customerId == null) {
            throw new IllegalArgumentException("order.error.customerId.notNull");
        }
    }
}