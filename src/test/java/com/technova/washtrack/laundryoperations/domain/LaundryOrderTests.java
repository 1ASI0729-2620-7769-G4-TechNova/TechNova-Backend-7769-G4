package com.technova.washtrack.laundryoperations.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.technova.washtrack.laundryoperations.domain.model.LaundryOrder;
import com.technova.washtrack.laundryoperations.domain.model.LaundryStatus;
import com.technova.washtrack.shared.exception.BusinessRuleException;

class LaundryOrderTests {

    private static final Instant T0 = Instant.parse("2026-10-01T15:30:00Z");

    @Test
    void stagesFollowAFixedOrder() {
        assertEquals(LaundryStatus.CLASSIFIED, LaundryStatus.RECEIVED.next().orElseThrow());
        assertEquals(LaundryStatus.WASHING, LaundryStatus.CLASSIFIED.next().orElseThrow());
        assertEquals(LaundryStatus.DRYING, LaundryStatus.WASHING.next().orElseThrow());
        assertEquals(LaundryStatus.IRONING, LaundryStatus.DRYING.next().orElseThrow());
        assertEquals(LaundryStatus.READY_FOR_DELIVERY, LaundryStatus.IRONING.next().orElseThrow());
        assertTrue(LaundryStatus.READY_FOR_DELIVERY.next().isEmpty());
    }

    @Test
    void receivesOrderInReceivedStageAndAdvancesSequentially() {
        LaundryOrder order = LaundryOrder.receive(1L, 101L, "ORD-000001", 4L, 3, T0);
        assertEquals(LaundryStatus.RECEIVED, order.status());
        assertFalse(order.isReadyForDelivery());

        Instant later = T0.plusSeconds(1800);
        assertEquals(LaundryStatus.CLASSIFIED, order.advance(later));
        assertEquals(later, order.updatedAt());
    }

    @Test
    void rejectsZeroGarmentsAndCannotAdvancePastReadyForDelivery() {
        assertThrows(BusinessRuleException.class,
                () -> LaundryOrder.receive(1L, 101L, "ORD-000001", 4L, 0, T0));

        LaundryOrder order = LaundryOrder.receive(1L, 101L, "ORD-000001", 4L, 3, T0);
        order.advance(T0);
        order.advance(T0);
        order.advance(T0);
        order.advance(T0);
        order.advance(T0);

        assertTrue(order.isReadyForDelivery());
        assertThrows(BusinessRuleException.class, () -> order.advance(T0));
    }
}