package com.technova.washtrack.delivery.infrastructure.persistence;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.technova.washtrack.delivery.domain.model.Delivery;
import com.technova.washtrack.delivery.domain.model.DeliveryStatus;
import com.technova.washtrack.delivery.domain.model.DeliveryType;
import com.technova.washtrack.delivery.domain.repository.DeliveryRepository;

/** Volatile storage used until the database is connected; replaced by a JPA adapter. */
@Repository
class InMemoryDeliveryRepository implements DeliveryRepository {

    private final Map<Long, Delivery> deliveries = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Long nextId() {
        return sequence.incrementAndGet();
    }

    @Override
    public Delivery save(Delivery delivery) {
        deliveries.put(delivery.id(), delivery);
        return delivery;
    }

    @Override
    public Optional<Delivery> findById(Long id) {
        return Optional.ofNullable(deliveries.get(id));
    }

    @Override
    public List<Delivery> search(Long orderId, DeliveryStatus status, DeliveryType type, Instant from, Instant to) {
        return deliveries.values().stream()
                .filter(d -> orderId == null || d.orderId().equals(orderId))
                .filter(d -> status == null || d.status() == status)
                .filter(d -> type == null || d.type() == type)
                .filter(d -> from == null || !d.scheduledAt().isBefore(from))
                .filter(d -> to == null || d.scheduledAt().isBefore(to))
                .sorted(Comparator.comparing(Delivery::scheduledAt))
                .toList();
    }
}
