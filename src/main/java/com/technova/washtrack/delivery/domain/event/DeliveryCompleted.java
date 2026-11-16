package com.technova.washtrack.delivery.domain.event;

import com.technova.washtrack.delivery.domain.model.DeliveryType;

/** A pickup or delivery finished successfully (a completed DELIVERY is the report's OrderDelivered). */
public record DeliveryCompleted(Long deliveryId, Long orderId, DeliveryType type) {
}
