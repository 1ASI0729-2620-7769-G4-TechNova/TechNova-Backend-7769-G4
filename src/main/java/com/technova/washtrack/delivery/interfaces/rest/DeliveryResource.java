package com.technova.washtrack.delivery.interfaces.rest;

import java.time.Instant;

import com.technova.washtrack.delivery.domain.model.Delivery;

public record DeliveryResource(Long id, Long orderId, String customerName, String type, String address,
        Instant scheduledAt, String status) {

    static DeliveryResource from(Delivery delivery) {
        return new DeliveryResource(delivery.id(), delivery.orderId(), delivery.customerName(),
                delivery.type().name(), delivery.address(), delivery.scheduledAt(), delivery.status().name());
    }
}
