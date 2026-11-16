package com.technova.washtrack.billing.interfaces.rest;

import java.time.Instant;

import com.technova.washtrack.billing.domain.model.Subscription;

public record SubscriptionResource(Long id, Long laundryId, Long planId, String status, Instant startDate,
        Instant endDate) {

    static SubscriptionResource from(Subscription subscription) {
        return new SubscriptionResource(subscription.id(), subscription.laundryId(), subscription.planId(),
                subscription.status().name(), subscription.startDate(), subscription.endDate());
    }
}
