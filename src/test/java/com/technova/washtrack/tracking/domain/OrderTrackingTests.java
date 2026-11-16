package com.technova.washtrack.tracking.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.technova.washtrack.shared.exception.BusinessRuleException;
import com.technova.washtrack.tracking.domain.model.OrderStage;
import com.technova.washtrack.tracking.domain.model.OrderTracking;

class OrderTrackingTests {

    private static final Instant T0 = Instant.parse("2026-10-01T14:30:00Z");

    @Test
    void stagesFollowAFixedOrder() {
        assertEquals(OrderStage.IN_PROCESS, OrderStage.RECEIVED.next().orElseThrow());
        assertEquals(OrderStage.READY_FOR_DELIVERY, OrderStage.IN_PROCESS.next().orElseThrow());
        assertEquals(OrderStage.DELIVERED, OrderStage.READY_FOR_DELIVERY.next().orElseThrow());
        assertEquals(true, OrderStage.DELIVERED.next().isEmpty());
    }

    @Test
    void startsReceivedAndAdvancesOneStageAtATime() {
        OrderTracking tracking = OrderTracking.start(1L, 101L, "ORD-000101", 4L, T0);
        assertEquals(OrderStage.RECEIVED, tracking.stage());

        Instant later = T0.plusSeconds(3600);
        assertEquals(OrderStage.IN_PROCESS, tracking.advance(later));
        assertEquals(later, tracking.updatedAt());
    }

    @Test
    void cannotAdvancePastDelivered() {
        OrderTracking tracking = OrderTracking.start(1L, 101L, "ORD-000101", 4L, T0);
        tracking.advance(T0);
        tracking.advance(T0);
        tracking.advance(T0);

        assertEquals(OrderStage.DELIVERED, tracking.stage());
        assertThrows(BusinessRuleException.class, () -> tracking.advance(T0));
    }
}
