package com.technova.washtrack.tracking.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.technova.washtrack.shared.exception.ResourceNotFoundException;
import com.technova.washtrack.tracking.domain.model.Notification;
import com.technova.washtrack.tracking.domain.model.OrderStage;

/** Each test uses its own recipient ids because the in-memory store is shared by the context. */
@SpringBootTest
class NotificationServiceTests {

    @Autowired
    private NotificationService notifications;

    @Autowired
    private TrackingService tracking;

    @Test
    void everyStageChangeNotifiesTheCustomer() {
        var started = tracking.start(4001L, "ORD-004001", 9001L);
        tracking.advance(started.id());

        List<Notification> inbox = notifications.list(9001L, false);

        assertEquals(2, inbox.size());
        assertEquals(OrderStage.IN_PROCESS, inbox.get(0).stage());
        assertEquals(OrderStage.RECEIVED, inbox.get(1).stage());
        assertEquals("ORD-004001", inbox.get(0).orderNumber());
        assertFalse(inbox.get(0).isRead());
    }

    @Test
    void marksNotificationsAsReadOneByOneOrAllAtOnce() {
        notifications.create(9002L, 4002L, "ORD-004002", OrderStage.RECEIVED);
        notifications.create(9002L, 4002L, "ORD-004002", OrderStage.IN_PROCESS);
        Notification newest = notifications.list(9002L, false).get(0);

        assertTrue(notifications.markRead(newest.id()).isRead());
        assertEquals(1, notifications.list(9002L, true).size());

        notifications.markAllRead(9002L);
        assertEquals(0, notifications.list(9002L, true).size());
        assertEquals(2, notifications.list(9002L, false).size());
    }

    @Test
    void unknownNotificationIsNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> notifications.markRead(999999L));
    }
}
