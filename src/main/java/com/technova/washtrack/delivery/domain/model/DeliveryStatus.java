package com.technova.washtrack.delivery.domain.model;

/** Lifecycle states of a pickup or delivery. */
public enum DeliveryStatus {
    SCHEDULED,
    ON_THE_WAY,
    COMPLETED,
    FAILED;

    /** SCHEDULED and ON_THE_WAY deliveries still occupy a time slot and block a duplicate request. */
    public boolean isActive() {
        return this == SCHEDULED || this == ON_THE_WAY;
    }
}
