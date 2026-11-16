package com.technova.washtrack.tracking.infrastructure.persistence;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.technova.washtrack.tracking.domain.model.StageChange;
import com.technova.washtrack.tracking.domain.repository.StageChangeRepository;

/** Volatile storage used until the database is connected; replaced by a JPA adapter. */
@Repository
class InMemoryStageChangeRepository implements StageChangeRepository {

    private final List<StageChange> changes = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Long nextId() {
        return sequence.incrementAndGet();
    }

    @Override
    public StageChange save(StageChange change) {
        changes.add(change);
        return change;
    }

    @Override
    public List<StageChange> findByOrderId(Long orderId) {
        return changes.stream().filter(c -> c.orderId().equals(orderId)).toList();
    }
}
