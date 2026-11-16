package com.technova.washtrack.orders.infrastructure.persistence.jpa;

import com.technova.washtrack.orders.domain.model.aggregates.Order;
import com.technova.washtrack.orders.domain.model.valueobjects.CustomerId;
import com.technova.washtrack.orders.domain.model.valueobjects.LaundryId;
import com.technova.washtrack.orders.domain.model.valueobjects.OrderNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for querying and persisting {@link Order} aggregates.
 * Provides collection-like access to Order roots, supporting queries
 * essential to maintaining domain invariants:
 * - {@code existsByOrderNumber()} tests the unique order number invariant
 * - {@code findByOrderNumber()} retrieves an order by its business number
 * - {@code findAllByCustomerId()} retrieves the current and previous orders of a customer
 * - {@code findAllByLaundryId()} retrieves the orders received by a laundry
 *
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    /**
     * Find an order by its order number.
     * @param orderNumber Order number
     */
    Optional<Order> findByOrderNumber(OrderNumber orderNumber);

    /**
     * Find all orders of a customer.
     * @param customerId Customer ID
     */
    List<Order> findAllByCustomerId(CustomerId customerId);

    /**
     * Find all orders received by a laundry.
     * @param laundryId Laundry ID
     */
    List<Order> findAllByLaundryId(LaundryId laundryId);

    /**
     * Check if an order exists by order number.
     * @param orderNumber Order number
     */
    boolean existsByOrderNumber(OrderNumber orderNumber);
}
