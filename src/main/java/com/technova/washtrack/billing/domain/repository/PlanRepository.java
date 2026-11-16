package com.technova.washtrack.billing.domain.repository;

import java.util.List;
import java.util.Optional;

import com.technova.washtrack.billing.domain.model.Plan;

public interface PlanRepository {

    List<Plan> findAll();

    Optional<Plan> findById(Long id);
}
