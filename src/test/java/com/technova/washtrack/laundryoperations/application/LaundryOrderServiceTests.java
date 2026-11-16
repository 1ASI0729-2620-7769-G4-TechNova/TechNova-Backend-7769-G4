package com.technova.washtrack.laundryoperations.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import com.technova.washtrack.laundryoperations.domain.event.LaundryOrderReadyForDelivery;
import com.technova.washtrack.laundryoperations.domain.event.LaundryOrderReceived;
import com.technova.washtrack.laundryoperations.domain.event.LaundryOrderStageAdvanced;
import com.technova.washtrack.laundryoperations.domain.model.LaundryOrder;
import com.technova.washtrack.laundryoperations.domain.model.LaundryStatus;
import com.technova.washtrack.shared.exception.BusinessRuleException;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;

/** Each test uses its own order and customer ids because the in-memory store is shared by the context. */
@SpringBootTest
@RecordApplicationEvents
class LaundryOrderServiceTests {

    @Autowired
    private LaundryOrderService service;

    @Autowired
    private ApplicationEvents events;

    @Test
    void receivesOrderAndPublishesReceivedEvent() {
        LaundryOrder received = service.receive(7001L, "ORD-007001", 8001L, 5);

        assertEquals(LaundryStatus.RECEIVED, received.status());
        assertEquals(5, received.garmentCount());
        assertEquals(1, events.stream(LaundryOrderReceived.class).filter(e -> e.orderId() == 7001L).count());
    }

    @Test
    void walksThroughAllStagesAndPublishesReadyForDeliveryAtTheEnd() {
        LaundryOrder order = service.receive(7002L, "ORD-007002", 8002L, 2);

        service.advance(order.id()); // CLASSIFIED
        service.advance(order.id()); // WASHING
        service.advance(order.id()); // DRYING
        service.advance(order.id()); // IRONING
        assertEquals(0, events.stream(LaundryOrderReadyForDelivery.class).filter(e -> e.orderId() == 7002L).count());

        LaundryOrder ready = service.advance(order.id()); // READY_FOR_DELIVERY
        assertEquals(LaundryStatus.READY_FOR_DELIVERY, ready.status());
        assertEquals(5, events.stream(LaundryOrderStageAdvanced.class).filter(e -> e.orderId() == 7002L).count());
        assertEquals(1, events.stream(LaundryOrderReadyForDelivery.class).filter(e -> e.orderId() == 7002L).count());
        assertThrows(BusinessRuleException.class, () -> service.advance(order.id()));
    }

    @Test
    void rejectsDuplicatesAndUnknownIds() {
        service.receive(7003L, "ORD-007003", 8003L, 1);

        assertThrows(BusinessRuleException.class, () -> service.receive(7003L, "ORD-007003", 8003L, 1));
        assertThrows(ResourceNotFoundException.class, () -> service.getById(999999L));
        assertThrows(ResourceNotFoundException.class, () -> service.advance(999999L));
    }
}