package com.technova.washtrack.laundryoperations.infrastructure.persistence;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.technova.washtrack.laundryoperations.domain.model.LaundryOrder;
import com.technova.washtrack.laundryoperations.domain.model.LaundryStatus;
import com.technova.washtrack.laundryoperations.domain.repository.LaundryOrderRepository;

/** Volatile storage used until the database is connected; replaced by a JPA adapter. */
@Repository
class InMemoryLaundryOrderRepository implements LaundryOrderRepository {

    private final Map<Long, LaundryOrder> laundryOrders = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Long nextId() {
        return sequence.incrementAndGet();
    }

    @Override
    public LaundryOrder save(LaundryOrder laundryOrder) {
        laundryOrders.put(laundryOrder.id(), laundryOrder);
        return laundryOrder;
    }

    @Override
    public Optional<LaundryOrder> findById(Long id) {
        return Optional.ofNullable(laundryOrders.get(id));
    }

    @Override
    public Optional<LaundryOrder> findByOrderId(Long orderId) {
        return laundryOrders.values().stream().filter(o -> o.orderId().equals(orderId)).findFirst();
    }

    @Override
    public List<LaundryOrder> search(LaundryStatus status, Long customerId) {
        return laundryOrders.values().stream()
                .filter(o -> status == null || o.status() == status)
                .filter(o -> customerId == null || o.customerId().equals(customerId))
                .sorted(Comparator.comparing(LaundryOrder::updatedAt).thenComparing(LaundryOrder::id))
                .toList();
    }
}