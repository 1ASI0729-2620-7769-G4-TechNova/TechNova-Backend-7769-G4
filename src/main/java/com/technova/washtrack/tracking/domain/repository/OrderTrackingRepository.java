package com.technova.washtrack.tracking.domain.repository;

import java.util.List;
import java.util.Optional;

import com.technova.washtrack.tracking.domain.model.OrderTracking;

public interface OrderTrackingRepository {

    Long nextId();

    OrderTracking save(OrderTracking tracking);

    Optional<OrderTracking> findById(Long id);

    Optional<OrderTracking> findByOrderId(Long orderId);

    /** Trackings of a customer (or of everyone when null), most recently updated first. */
    List<OrderTracking> search(Long customerId);
}
