package com.technova.washtrack.tracking.domain.event;

import com.technova.washtrack.tracking.domain.model.OrderStage;

/** An order entered a new stage; the customer must be told. */
public record OrderStatusUpdated(Long orderId, String orderNumber, Long customerId, OrderStage stage) {
}
