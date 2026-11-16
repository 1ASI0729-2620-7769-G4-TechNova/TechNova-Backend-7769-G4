package com.technova.washtrack.delivery.domain.event;

import com.technova.washtrack.delivery.domain.model.DeliveryType;

/** A pickup or delivery could not be carried out. */
public record DeliveryFailed(Long deliveryId, Long orderId, DeliveryType type, String reason) {
}
