package com.technova.washtrack.tracking.domain.repository;

import java.util.List;

import com.technova.washtrack.tracking.domain.model.StageChange;

public interface StageChangeRepository {

    Long nextId();

    StageChange save(StageChange change);

    /** History of an order, oldest first. */
    List<StageChange> findByOrderId(Long orderId);
}
