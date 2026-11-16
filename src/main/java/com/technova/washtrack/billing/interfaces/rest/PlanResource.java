package com.technova.washtrack.billing.interfaces.rest;

import java.math.BigDecimal;
import java.util.List;

import com.technova.washtrack.billing.domain.model.Plan;

public record PlanResource(Long id, String name, BigDecimal price, String currency, List<String> features) {

    static PlanResource from(Plan plan) {
        return new PlanResource(plan.id(), plan.name().name(), plan.price(), plan.currency(), plan.features());
    }
}
