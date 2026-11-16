package com.technova.washtrack.tracking.infrastructure.persistence;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.technova.washtrack.tracking.domain.model.OrderTracking;
import com.technova.washtrack.tracking.domain.repository.OrderTrackingRepository;

/** Volatile storage used until the database is connected; replaced by a JPA adapter. */
@Repository
class InMemoryOrderTrackingRepository implements OrderTrackingRepository {

    private final Map<Long, OrderTracking> trackings = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Long nextId() {
        return sequence.incrementAndGet();
    }

    @Override
    public OrderTracking save(OrderTracking tracking) {
        trackings.put(tracking.id(), tracking);
        return tracking;
    }

    @Override
    public Optional<OrderTracking> findById(Long id) {
        return Optional.ofNullable(trackings.get(id));
    }

    @Override
    public Optional<OrderTracking> findByOrderId(Long orderId) {
        return trackings.values().stream().filter(t -> t.orderId().equals(orderId)).findFirst();
    }

    @Override
    public List<OrderTracking> search(Long customerId) {
        return trackings.values().stream()
                .filter(t -> customerId == null || t.customerId().equals(customerId))
                .sorted(Comparator.comparing(OrderTracking::updatedAt).thenComparing(OrderTracking::id).reversed())
                .toList();
    }
}
