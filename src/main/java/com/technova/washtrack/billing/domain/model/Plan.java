package com.technova.washtrack.billing.domain.model;

import java.math.BigDecimal;
import java.util.List;

/** A monthly subscription plan and the features it enables for a laundry. */
public record Plan(Long id, PlanName name, BigDecimal price, String currency, List<String> features) {

    public Plan {
        features = List.copyOf(features);
    }

    public boolean hasFeature(String feature) {
        return features.contains(feature);
    }
}
