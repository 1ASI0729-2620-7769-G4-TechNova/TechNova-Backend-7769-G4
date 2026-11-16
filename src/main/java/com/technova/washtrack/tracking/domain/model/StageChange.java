package com.technova.washtrack.tracking.domain.model;

import java.time.Instant;

/** One entry of an order's history: the order entered {@code stage} at {@code changedAt}. */
public record StageChange(Long id, Long orderId, OrderStage stage, Instant changedAt) {
}
