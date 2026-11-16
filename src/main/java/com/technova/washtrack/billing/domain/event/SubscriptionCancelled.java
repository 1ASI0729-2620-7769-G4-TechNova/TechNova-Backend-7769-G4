package com.technova.washtrack.billing.domain.event;

/** A laundry cancelled its subscription. */
public record SubscriptionCancelled(Long subscriptionId, Long laundryId) {
}
