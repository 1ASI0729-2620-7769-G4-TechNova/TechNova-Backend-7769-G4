package com.technova.washtrack.delivery.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import com.technova.washtrack.delivery.domain.event.DeliveryCompleted;
import com.technova.washtrack.delivery.domain.event.DeliveryFailed;
import com.technova.washtrack.delivery.domain.model.Delivery;
import com.technova.washtrack.delivery.domain.model.DeliveryType;

@SpringBootTest
@RecordApplicationEvents
class DeliveryEventsTests {

    @Autowired
    private DeliveryService deliveries;

    @Autowired
    private DriverService drivers;

    @Autowired
    private ApplicationEvents events;

    private Delivery scheduled(long orderId, String when) {
        return deliveries.schedule(orderId, "Ana", DeliveryType.DELIVERY, "Av. Larco 123", Instant.parse(when));
    }

    @Test
    void completingPublishesDeliveryCompleted() {
        Delivery delivery = scheduled(1201L, "2099-07-01T10:00:00Z");
        deliveries.assignDriver(delivery.id(), drivers.register("Events Driver").id());
        deliveries.start(delivery.id());

        deliveries.complete(delivery.id());

        assertEquals(1, events.stream(DeliveryCompleted.class)
                .filter(e -> e.orderId() == 1201L && e.type() == DeliveryType.DELIVERY).count());
    }

    @Test
    void failingPublishesDeliveryFailed() {
        Delivery delivery = scheduled(1202L, "2099-07-02T10:00:00Z");

        deliveries.fail(delivery.id(), "Customer rescheduled");

        assertEquals(1, events.stream(DeliveryFailed.class)
                .filter(e -> e.orderId() == 1202L && e.reason().equals("Customer rescheduled")).count());
    }
}
