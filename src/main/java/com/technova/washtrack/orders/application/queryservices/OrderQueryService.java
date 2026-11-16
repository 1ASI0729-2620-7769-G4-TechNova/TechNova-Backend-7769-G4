package com.technova.washtrack.orders.application.queryservices;

import com.technova.washtrack.orders.domain.model.aggregates.Order;
import com.technova.washtrack.orders.domain.model.queries.GetOrderByIdQuery;
import com.technova.washtrack.orders.domain.model.queries.GetOrdersByCustomerIdQuery;
import com.technova.washtrack.orders.domain.model.queries.GetOrdersByLaundryIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application service contract providing read access to orders.
 * It exposes query operations used by the interface layer to retrieve orders
 * without leaking persistence details.
 *
 */
public interface OrderQueryService {
    /**
     * Retrieves an order by its identifier.
     *
     */
    Optional<Order> handle(GetOrderByIdQuery query);

    /**
     * Retrieves all the orders of a customer.
     *
     */
    List<Order> handle(GetOrdersByCustomerIdQuery query);

    /**
     * Retrieves all the orders received by a laundry.
     *
     */
    List<Order> handle(GetOrdersByLaundryIdQuery query);
}
