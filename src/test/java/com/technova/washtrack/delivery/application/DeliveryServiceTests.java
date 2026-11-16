package com.technova.washtrack.delivery.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.technova.washtrack.delivery.domain.model.Delivery;
import com.technova.washtrack.delivery.domain.model.DeliveryStatus;
import com.technova.washtrack.delivery.domain.model.DeliveryType;
import com.technova.washtrack.shared.exception.BusinessRuleException;
import com.technova.washtrack.shared.exception.ConflictException;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;
import com.technova.washtrack.support.MutableClock;

/** Each test uses its own order ids and time slots because the in-memory store is shared across the context. */
@SpringBootTest
@Import(DeliveryServiceTests.FrozenTime.class)
class DeliveryServiceTests {

    @TestConfiguration
    static class FrozenTime {

        @Bean
        @Primary
        Clock testClock() {
            return new MutableClock(Instant.parse("2026-09-14T00:00:00Z"));
        }
    }

    @Autowired
    private DeliveryService deliveries;

    private Delivery schedule(long orderId, DeliveryType type, String when) {
        return deliveries.schedule(orderId, "Ana Martinez", type, "Av. Larco 123, Miraflores", Instant.parse(when));
    }

    @Test
    void schedulesAPickup() {
        Delivery delivery = schedule(801L, DeliveryType.PICKUP, "2026-10-02T15:00:00Z");

        assertEquals(DeliveryStatus.SCHEDULED, delivery.status());
        assertEquals(delivery.id(), deliveries.getById(delivery.id()).id());
    }

    @Test
    void rejectsPastSchedule() {
        assertThrows(BusinessRuleException.class, () -> schedule(802L, DeliveryType.PICKUP, "2026-09-13T10:00:00Z"));
    }

    @Test
    void anOrderCannotHaveTwoActiveRequestsOfTheSameType() {
        schedule(803L, DeliveryType.PICKUP, "2026-10-03T10:00:00Z");

        assertThrows(BusinessRuleException.class, () -> schedule(803L, DeliveryType.PICKUP, "2026-10-04T10:00:00Z"));
        assertEquals(DeliveryStatus.SCHEDULED, schedule(803L, DeliveryType.DELIVERY, "2026-10-05T10:00:00Z").status());
    }

    @Test
    void aFullTimeSlotIsAConflict() {
        schedule(811L, DeliveryType.PICKUP, "2026-11-01T10:00:00Z");
        schedule(812L, DeliveryType.PICKUP, "2026-11-01T10:20:00Z");
        schedule(813L, DeliveryType.PICKUP, "2026-11-01T10:59:00Z");

        assertThrows(ConflictException.class, () -> schedule(814L, DeliveryType.PICKUP, "2026-11-01T10:30:00Z"));
        assertEquals(DeliveryStatus.SCHEDULED, schedule(814L, DeliveryType.PICKUP, "2026-11-01T11:00:00Z").status());
    }

    @Test
    void unknownDeliveryIsNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> deliveries.getById(999999L));
    }
}
