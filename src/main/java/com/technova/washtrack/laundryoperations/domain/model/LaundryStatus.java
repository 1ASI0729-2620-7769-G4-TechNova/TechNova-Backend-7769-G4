package com.technova.washtrack.laundryoperations.domain.model;

import java.util.Optional;

/** Stages of garment processing inside the laundry, in the only order they can happen. */
public enum LaundryStatus {
    RECEIVED,
    CLASSIFIED,
    WASHING,
    DRYING,
    IRONING,
    READY_FOR_DELIVERY;

    public Optional<LaundryStatus> next() {
        return ordinal() + 1 < values().length ? Optional.of(values()[ordinal() + 1]) : Optional.empty();
    }
}
