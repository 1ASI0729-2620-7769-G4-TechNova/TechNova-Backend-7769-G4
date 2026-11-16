package com.technova.washtrack.tracking.domain.model;

import java.time.Instant;

/** Message for a customer telling them their order entered a new stage. */
public class Notification {

    private final Long id;
    private final Long recipientId;
    private final Long orderId;
    private final String orderNumber;
    private final OrderStage stage;
    private final Instant createdAt;
    private boolean read;

    public Notification(Long id, Long recipientId, Long orderId, String orderNumber, OrderStage stage,
            Instant createdAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.stage = stage;
        this.createdAt = createdAt;
    }

    public void markRead() {
        read = true;
    }

    public Long id() {
        return id;
    }

    public Long recipientId() {
        return recipientId;
    }

    public Long orderId() {
        return orderId;
    }

    public String orderNumber() {
        return orderNumber;
    }

    public OrderStage stage() {
        return stage;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public boolean isRead() {
        return read;
    }
}
