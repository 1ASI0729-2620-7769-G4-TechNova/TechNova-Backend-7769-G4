package com.technova.washtrack.tracking.domain.model;

import java.time.Instant;

import com.technova.washtrack.shared.exception.BusinessRuleException;

/** Aggregate with the current stage of an order, the view the customer consults. */
public class OrderTracking {

    private final Long id;
    private final Long orderId;
    private final String orderNumber;
    private final Long customerId;
    private OrderStage stage;
    private Instant updatedAt;

    private OrderTracking(Long id, Long orderId, String orderNumber, Long customerId, Instant now) {
        this.id = id;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.stage = OrderStage.RECEIVED;
        this.updatedAt = now;
    }

    public static OrderTracking start(Long id, Long orderId, String orderNumber, Long customerId, Instant now) {
        return new OrderTracking(id, orderId, orderNumber, customerId, now);
    }

    /** Moves the order to the next stage; stages cannot be skipped or repeated. */
    public OrderStage advance(Instant now) {
        stage = stage.next().orElseThrow(() -> new BusinessRuleException("Order " + orderNumber + " was already delivered"));
        updatedAt = now;
        return stage;
    }

    public Long id() {
        return id;
    }

    public Long orderId() {
        return orderId;
    }

    public String orderNumber() {
        return orderNumber;
    }

    public Long customerId() {
        return customerId;
    }

    public OrderStage stage() {
        return stage;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
