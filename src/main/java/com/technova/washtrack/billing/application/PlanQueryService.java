package com.technova.washtrack.billing.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.technova.washtrack.billing.domain.model.Plan;
import com.technova.washtrack.billing.domain.repository.PlanRepository;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;

@Service
public class PlanQueryService {

    private final PlanRepository plans;

    PlanQueryService(PlanRepository plans) {
        this.plans = plans;
    }

    public List<Plan> findAll() {
        return plans.findAll();
    }

    public Plan getById(Long id) {
        return plans.findById(id).orElseThrow(() -> new ResourceNotFoundException("Plan", id));
    }
}
