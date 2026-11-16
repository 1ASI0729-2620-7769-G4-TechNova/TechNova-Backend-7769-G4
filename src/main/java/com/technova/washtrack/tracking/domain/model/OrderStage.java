package com.technova.washtrack.tracking.domain.model;

import java.util.Optional;

/** Stages an order goes through, in the only order they can happen. */
public enum OrderStage {
    RECEIVED,
    IN_PROCESS,
    READY_FOR_DELIVERY,
    DELIVERED;

    public Optional<OrderStage> next() {
        return ordinal() + 1 < values().length ? Optional.of(values()[ordinal() + 1]) : Optional.empty();
    }
}
