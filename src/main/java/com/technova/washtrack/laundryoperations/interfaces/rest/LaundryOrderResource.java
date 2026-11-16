package com.technova.washtrack.laundryoperations.interfaces.rest;

import java.time.Instant;

import com.technova.washtrack.laundryoperations.domain.model.LaundryOrder;

public record LaundryOrderResource(Long id, Long orderId, String orderNumber, Long customerId, int garmentCount,
                                   String status, Instant receivedAt, Instant updatedAt) {

    static LaundryOrderResource from(LaundryOrder order) {
        return new LaundryOrderResource(order.id(), order.orderId(), order.orderNumber(), order.customerId(),
                order.garmentCount(), order.status().name(), order.receivedAt(), order.updatedAt());
    }
}
