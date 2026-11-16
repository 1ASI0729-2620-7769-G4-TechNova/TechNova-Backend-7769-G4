package com.technova.washtrack.billing.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.technova.washtrack.billing.domain.model.Plan;
import com.technova.washtrack.billing.domain.model.PlanName;
import com.technova.washtrack.billing.domain.model.Subscription;
import com.technova.washtrack.billing.domain.model.SubscriptionStatus;
import com.technova.washtrack.shared.exception.BusinessRuleException;

class SubscriptionTests {

    private static final Instant START = Instant.parse("2026-09-14T00:00:00Z");
    private static final Plan FREE = new Plan(1L, PlanName.FREE, BigDecimal.ZERO, "PEN", List.of("orders"));
    private static final Plan BASIC = new Plan(2L, PlanName.BASIC, BigDecimal.valueOf(49), "PEN", List.of("orders"));
    private static final Plan ADVANCED = new Plan(3L, PlanName.ADVANCED, BigDecimal.valueOf(99), "PEN", List.of("orders"));

    @Test
    void paidPlanCoversOneMonth() {
        Subscription subscription = Subscription.subscribe(1L, 10L, BASIC, START);

        assertEquals(SubscriptionStatus.ACTIVE, subscription.status());
        assertEquals(2L, subscription.planId());
        assertEquals(Instant.parse("2026-10-14T00:00:00Z"), subscription.endDate());
    }

    @Test
    void freePlanCoversOneYear() {
        Subscription subscription = Subscription.subscribe(1L, 10L, FREE, START);

        assertEquals(Instant.parse("2027-09-14T00:00:00Z"), subscription.endDate());
    }

    @Test
    void changingPlanKeepsThePeriod() {
        Subscription subscription = Subscription.subscribe(1L, 10L, BASIC, START);

        subscription.changePlan(ADVANCED);

        assertEquals(3L, subscription.planId());
        assertEquals(Instant.parse("2026-10-14T00:00:00Z"), subscription.endDate());
    }

    @Test
    void cannotChangeToTheSamePlanOrWhenExpired() {
        Subscription subscription = Subscription.subscribe(1L, 10L, BASIC, START);
        assertThrows(BusinessRuleException.class, () -> subscription.changePlan(BASIC));

        subscription.refresh(Instant.parse("2026-11-01T00:00:00Z"));
        assertThrows(BusinessRuleException.class, () -> subscription.changePlan(ADVANCED));
    }

    @Test
    void expiresOnlyOnceTheEndDateIsReached() {
        Subscription subscription = Subscription.subscribe(1L, 10L, BASIC, START);

        subscription.refresh(Instant.parse("2026-10-13T23:59:59Z"));
        assertTrue(subscription.isActive());

        subscription.refresh(Instant.parse("2026-10-14T00:00:00Z"));
        assertEquals(SubscriptionStatus.EXPIRED, subscription.status());
    }
}
