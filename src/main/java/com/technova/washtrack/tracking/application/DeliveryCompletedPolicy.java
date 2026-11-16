package com.technova.washtrack.tracking.application;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.technova.washtrack.delivery.domain.event.DeliveryCompleted;
import com.technova.washtrack.delivery.domain.model.DeliveryType;

/** Policy "when a home delivery is completed (OrderDelivered), reflect it in the order's tracking". */
@Component
class DeliveryCompletedPolicy {

    private final TrackingService tracking;

    DeliveryCompletedPolicy(TrackingService tracking) {
        this.tracking = tracking;
    }

    @EventListener
    void onDeliveryCompleted(DeliveryCompleted event) {
        if (event.type() == DeliveryType.DELIVERY) {
            tracking.markDelivered(event.orderId());
        }
    }
}
