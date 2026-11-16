package com.technova.washtrack.billing.infrastructure.persistence;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.technova.washtrack.billing.domain.model.Subscription;
import com.technova.washtrack.billing.domain.repository.SubscriptionRepository;

/** Volatile storage used until the database is connected; replaced by a JPA adapter. */
@Repository
class InMemorySubscriptionRepository implements SubscriptionRepository {

    private final Map<Long, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Long nextId() {
        return sequence.incrementAndGet();
    }

    @Override
    public Subscription save(Subscription subscription) {
        subscriptions.put(subscription.id(), subscription);
        return subscription;
    }

    @Override
    public Optional<Subscription> findById(Long id) {
        return Optional.ofNullable(subscriptions.get(id));
    }

    @Override
    public List<Subscription> findByLaundryId(Long laundryId) {
        return subscriptions.values().stream()
                .filter(s -> s.laundryId().equals(laundryId))
                .sorted(Comparator.comparing(Subscription::id).reversed())
                .toList();
    }
}
