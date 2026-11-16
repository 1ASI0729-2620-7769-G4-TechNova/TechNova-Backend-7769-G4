package com.technova.washtrack.tracking.interfaces.rest;

import java.time.Instant;

import com.technova.washtrack.tracking.domain.model.OrderTracking;

public record OrderTrackingResource(Long id, Long orderId, String orderNumber, Long customerId, String stage,
        Instant updatedAt) {

    static OrderTrackingResource from(OrderTracking tracking) {
        return new OrderTrackingResource(tracking.id(), tracking.orderId(), tracking.orderNumber(),
                tracking.customerId(), tracking.stage().name(), tracking.updatedAt());
    }
}
