package com.technova.washtrack.billing.domain.event;

import java.time.Instant;

/** A subscription was renewed and now runs until {@code endDate}. */
public record SubscriptionRenewed(Long subscriptionId, Long laundryId, Instant endDate) {
}
