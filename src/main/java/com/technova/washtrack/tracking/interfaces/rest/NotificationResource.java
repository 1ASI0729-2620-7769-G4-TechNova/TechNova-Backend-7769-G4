package com.technova.washtrack.tracking.interfaces.rest;

import java.time.Instant;

import com.technova.washtrack.tracking.domain.model.Notification;

public record NotificationResource(Long id, Long recipientId, Long orderId, String orderNumber, String stage,
        Instant createdAt, boolean read) {

    static NotificationResource from(Notification notification) {
        return new NotificationResource(notification.id(), notification.recipientId(), notification.orderId(),
                notification.orderNumber(), notification.stage().name(), notification.createdAt(),
                notification.isRead());
    }
}
