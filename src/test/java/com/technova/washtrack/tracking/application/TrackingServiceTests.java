package com.technova.washtrack.tracking.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import com.technova.washtrack.shared.exception.BusinessRuleException;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;
import com.technova.washtrack.tracking.domain.event.OrderStatusUpdated;
import com.technova.washtrack.tracking.domain.model.OrderStage;
import com.technova.washtrack.tracking.domain.model.OrderTracking;
import com.technova.washtrack.tracking.domain.model.StageChange;

/** Each test uses its own order and customer ids because the in-memory store is shared by the context. */
@SpringBootTest
@RecordApplicationEvents
class TrackingServiceTests {

    @Autowired
    private TrackingService tracking;

    @Autowired
    private ApplicationEvents events;

    @Test
    void startsReceivedAndRecordsHistoryAndEvent() {
        OrderTracking started = tracking.start(2001L, "ORD-002001", 7001L);

        assertEquals(OrderStage.RECEIVED, started.stage());
        assertEquals(List.of(OrderStage.RECEIVED), tracking.history(2001L).stream().map(StageChange::stage).toList());
        assertEquals(1, events.stream(OrderStatusUpdated.class).filter(e -> e.orderId() == 2001L).count());
    }

    @Test
    void walksThroughEveryStageAndStopsAtDelivered() {
        OrderTracking started = tracking.start(2002L, "ORD-002002", 7002L);

        tracking.advance(started.id());
        tracking.advance(started.id());
        assertEquals(OrderStage.DELIVERED, tracking.advanceOrder(2002L).stage());

        assertEquals(List.of(OrderStage.RECEIVED, OrderStage.IN_PROCESS, OrderStage.READY_FOR_DELIVERY,
                OrderStage.DELIVERED), tracking.history(2002L).stream().map(StageChange::stage).toList());
        assertThrows(BusinessRuleException.class, () -> tracking.advance(started.id()));
    }

    @Test
    void rejectsDuplicatesAndUnknownIds() {
        tracking.start(2003L, "ORD-002003", 7003L);

        assertThrows(BusinessRuleException.class, () -> tracking.start(2003L, "ORD-002003", 7003L));
        assertThrows(ResourceNotFoundException.class, () -> tracking.getById(999999L));
        assertThrows(ResourceNotFoundException.class, () -> tracking.advanceOrder(999999L));
    }

    @Test
    void listsATrackingsOfACustomerNewestFirst() {
        tracking.start(2004L, "ORD-002004", 7004L);
        OrderTracking second = tracking.start(2005L, "ORD-002005", 7004L);

        assertEquals(second.id(), tracking.search(7004L).get(0).id());
        assertEquals(2, tracking.search(7004L).size());
        assertEquals(0, tracking.search(7999L).size());
    }
}
