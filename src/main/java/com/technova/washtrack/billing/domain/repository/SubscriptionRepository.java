package com.technova.washtrack.billing.domain.repository;

import java.util.List;
import java.util.Optional;

import com.technova.washtrack.billing.domain.model.Subscription;

public interface SubscriptionRepository {

    Long nextId();

    Subscription save(Subscription subscription);

    Optional<Subscription> findById(Long id);

    /** Subscriptions of a laundry, newest first. */
    List<Subscription> findByLaundryId(Long laundryId);
}
