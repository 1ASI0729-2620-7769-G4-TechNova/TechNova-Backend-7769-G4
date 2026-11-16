package com.technova.washtrack.tracking.interfaces.rest;

import java.time.Instant;

import com.technova.washtrack.tracking.domain.model.StageChange;

public record StageChangeResource(Long id, Long orderId, String stage, Instant changedAt) {

    static StageChangeResource from(StageChange change) {
        return new StageChangeResource(change.id(), change.orderId(), change.stage().name(), change.changedAt());
    }
}
