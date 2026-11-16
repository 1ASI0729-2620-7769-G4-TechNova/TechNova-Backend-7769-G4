package com.technova.washtrack.tracking.application;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.technova.washtrack.tracking.domain.event.OrderStatusUpdated;

/** Policy "when OrderStatusUpdated, send a notification to the customer". */
@Component
class NotificationPolicy {

    private final NotificationService notifications;

    NotificationPolicy(NotificationService notifications) {
        this.notifications = notifications;
    }

    @EventListener
    void onOrderStatusUpdated(OrderStatusUpdated event) {
        notifications.create(event.customerId(), event.orderId(), event.orderNumber(), event.stage());
    }
}
