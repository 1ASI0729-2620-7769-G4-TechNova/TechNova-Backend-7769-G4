package com.technova.washtrack.laundryoperations.domain.repository;

import java.util.List;
import java.util.Optional;

import com.technova.washtrack.laundryoperations.domain.model.LaundryOrder;
import com.technova.washtrack.laundryoperations.domain.model.LaundryStatus;

public interface LaundryOrderRepository {

    Long nextId();

    LaundryOrder save(LaundryOrder laundryOrder);

    Optional<LaundryOrder> findById(Long id);

    Optional<LaundryOrder> findByOrderId(Long orderId);

    /**
     * Searches laundry orders sorted by {@code updatedAt} ascending (oldest update first).
     * Null criteria mean "any".
     */
    List<LaundryOrder> search(LaundryStatus status, Long customerId);
}
