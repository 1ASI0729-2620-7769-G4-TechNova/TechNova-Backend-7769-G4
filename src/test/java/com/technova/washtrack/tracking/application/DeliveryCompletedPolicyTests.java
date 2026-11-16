package com.technova.washtrack.tracking.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.technova.washtrack.delivery.application.DeliveryService;
import com.technova.washtrack.delivery.application.DriverService;
import com.technova.washtrack.delivery.domain.model.Delivery;
import com.technova.washtrack.delivery.domain.model.DeliveryType;
import com.technova.washtrack.tracking.domain.model.OrderStage;
import com.technova.washtrack.tracking.domain.model.OrderTracking;

/** Delivery and Tracking talk only through events; ids and 2099 dates are unique to this class. */
@SpringBootTest
class DeliveryCompletedPolicyTests {

    @Autowired
    private TrackingService tracking;

    @Autowired
    private NotificationService notifications;

    @Autowired
    private DeliveryService deliveries;

    @Autowired
    private DriverService drivers;

    private void complete(long orderId, DeliveryType type, String when) {
        Delivery delivery = deliveries.schedule(orderId, "Ana", type, "Av. Larco 123", Instant.parse(when));
        deliveries.assignDriver(delivery.id(), drivers.register("Policy Driver " + orderId).id());
        deliveries.start(delivery.id());
        deliveries.complete(delivery.id());
    }

    @Test
    void completedHomeDeliveryMarksTheOrderDeliveredAndNotifiesTheCustomer() {
        OrderTracking started = tracking.start(6001L, "ORD-006001", 9501L);
        tracking.advance(started.id());
        tracking.advance(started.id());

        complete(6001L, DeliveryType.DELIVERY, "2099-08-01T10:00:00Z");

        assertEquals(OrderStage.DELIVERED, tracking.getById(started.id()).stage());
        assertEquals(OrderStage.DELIVERED, notifications.list(9501L, false).get(0).stage());
    }

    @Test
    void doesNothingForPickupsOrOrdersNotReadyForDelivery() {
        OrderTracking pickup = tracking.start(6002L, "ORD-006002", 9502L);
        tracking.advance(pickup.id());
        tracking.advance(pickup.id());
        complete(6002L, DeliveryType.PICKUP, "2099-08-02T10:00:00Z");
        assertEquals(OrderStage.READY_FOR_DELIVERY, tracking.getById(pickup.id()).stage());

        OrderTracking early = tracking.start(6003L, "ORD-006003", 9503L);
        complete(6003L, DeliveryType.DELIVERY, "2099-08-03T10:00:00Z");
        assertEquals(OrderStage.RECEIVED, tracking.getById(early.id()).stage());

        complete(6004L, DeliveryType.DELIVERY, "2099-08-04T10:00:00Z");
    }
}
