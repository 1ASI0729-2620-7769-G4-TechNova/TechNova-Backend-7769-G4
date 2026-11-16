package com.technova.washtrack.orders.domain.model.valueobjects;

/**
 * Lifecycle states of an order inside the Order Management bounded context.
 * <p>
 * Allowed transitions handled by this bounded context:
 * - {@code DRAFT -> CONFIRMED}: the order has at least one item and is confirmed by the laundry staff
 * - {@code DRAFT -> CANCELLED} and {@code CONFIRMED -> CANCELLED}: the order has not entered processing yet
 * <p>
 * {@code IN_PROCESS}, {@code READY_FOR_DELIVERY} and {@code COMPLETED} are reached through the
 * Laundry Operations and Delivery Management bounded contexts.
 *
 */
public enum OrderStatus {
    /** Order registered, items can still be added. */
    DRAFT,
    /** Order confirmed by the laundry staff, items can no longer be added. */
    CONFIRMED,
    /** Garments are being processed by the laundry. */
    IN_PROCESS,
    /** Garments are ready to be picked up or delivered. */
    READY_FOR_DELIVERY,
    /** Order delivered to the customer and closed. */
    COMPLETED,
    /** Order cancelled before entering processing. */
    CANCELLED
}