package com.technova.washtrack.billing.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.technova.washtrack.billing.domain.model.Plan;
import com.technova.washtrack.billing.domain.model.PlanName;
import com.technova.washtrack.billing.domain.repository.PlanRepository;

/** Fixed catalogue of plans (free, basic S/ 49, advanced S/ 99). Replaced by JPA once the database is connected. */
@Repository
class InMemoryPlanRepository implements PlanRepository {

    private static final String CURRENCY = "PEN";

    private final List<Plan> plans = List.of(
            new Plan(1L, PlanName.FREE, BigDecimal.ZERO, CURRENCY, List.of("orders")),
            new Plan(2L, PlanName.BASIC, BigDecimal.valueOf(49), CURRENCY,
                    List.of("orders", "reports", "digital-payments")),
            new Plan(3L, PlanName.ADVANCED, BigDecimal.valueOf(99), CURRENCY,
                    List.of("orders", "reports", "digital-payments", "advanced-history", "delivery")));

    @Override
    public List<Plan> findAll() {
        return plans;
    }

    @Override
    public Optional<Plan> findById(Long id) {
        return plans.stream().filter(plan -> plan.id().equals(id)).findFirst();
    }
}
