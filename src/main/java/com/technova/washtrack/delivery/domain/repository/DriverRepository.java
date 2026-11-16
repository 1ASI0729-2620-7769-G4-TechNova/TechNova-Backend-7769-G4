package com.technova.washtrack.delivery.domain.repository;

import java.util.List;
import java.util.Optional;

import com.technova.washtrack.delivery.domain.model.Driver;

public interface DriverRepository {

    Long nextId();

    Driver save(Driver driver);

    Optional<Driver> findById(Long id);

    /** All drivers ordered by name. */
    List<Driver> findAll();
}
