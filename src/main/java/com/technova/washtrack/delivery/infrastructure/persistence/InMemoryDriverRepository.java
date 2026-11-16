package com.technova.washtrack.delivery.infrastructure.persistence;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.technova.washtrack.delivery.domain.model.Driver;
import com.technova.washtrack.delivery.domain.repository.DriverRepository;

/** Volatile storage used until the database is connected; replaced by a JPA adapter. */
@Repository
class InMemoryDriverRepository implements DriverRepository {

    private final Map<Long, Driver> drivers = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Long nextId() {
        return sequence.incrementAndGet();
    }

    @Override
    public Driver save(Driver driver) {
        drivers.put(driver.id(), driver);
        return driver;
    }

    @Override
    public Optional<Driver> findById(Long id) {
        return Optional.ofNullable(drivers.get(id));
    }

    @Override
    public List<Driver> findAll() {
        return drivers.values().stream().sorted(Comparator.comparing(Driver::name)).toList();
    }
}
