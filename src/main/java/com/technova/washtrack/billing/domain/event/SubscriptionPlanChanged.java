package com.technova.washtrack.billing.domain.event;

/** A laundry moved to another plan; the features of {@code newPlanId} are now enabled. */
public record SubscriptionPlanChanged(Long subscriptionId, Long laundryId, Long oldPlanId, Long newPlanId) {
}
