package com.technova.washtrack.billing.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.technova.washtrack.billing.domain.model.Plan;
import com.technova.washtrack.billing.domain.model.PlanName;

class PlanTests {

    @Test
    void reportsWhichFeaturesItEnables() {
        Plan plan = new Plan(2L, PlanName.BASIC, BigDecimal.valueOf(49), "PEN", List.of("orders", "reports"));

        assertTrue(plan.hasFeature("reports"));
        assertFalse(plan.hasFeature("delivery"));
    }

    @Test
    void featuresAreImmutable() {
        Plan plan = new Plan(1L, PlanName.FREE, BigDecimal.ZERO, "PEN", new ArrayList<>(List.of("orders")));

        assertThrows(UnsupportedOperationException.class, () -> plan.features().add("delivery"));
    }
}
