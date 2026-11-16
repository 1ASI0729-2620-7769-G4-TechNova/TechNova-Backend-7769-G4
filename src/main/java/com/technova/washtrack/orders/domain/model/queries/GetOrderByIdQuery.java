package com.technova.washtrack.orders.domain.model.queries;

/**
 * Query for retrieving an order by its identifier.
 *
 * @param orderId order identifier
 *
 */
public record GetOrderByIdQuery(Long orderId) {
    /**
     * Validates the query.
     *
     * @throws IllegalArgumentException if {@code orderId} is null or not greater than 0
     */
    public GetOrderByIdQuery {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("order.error.id.invalid");
        }
    }
}