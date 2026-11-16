package com.technova.washtrack.tracking.application;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;

import com.technova.washtrack.shared.exception.ResourceNotFoundException;
import com.technova.washtrack.tracking.domain.model.Notification;
import com.technova.washtrack.tracking.domain.model.OrderStage;
import com.technova.washtrack.tracking.domain.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notifications;
    private final Clock clock;

    NotificationService(NotificationRepository notifications, Clock clock) {
        this.notifications = notifications;
        this.clock = clock;
    }

    public Notification create(Long recipientId, Long orderId, String orderNumber, OrderStage stage) {
        return notifications.save(new Notification(notifications.nextId(), recipientId, orderId, orderNumber, stage,
                clock.instant()));
    }

    /** Notifications of a recipient, newest first; {@code onlyUnread} hides the ones already read. */
    public List<Notification> list(Long recipientId, boolean onlyUnread) {
        return notifications.findByRecipientId(recipientId).stream().filter(n -> !onlyUnread || !n.isRead()).toList();
    }

    public Notification markRead(Long id) {
        Notification notification = notifications.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        notification.markRead();
        return notifications.save(notification);
    }

    public void markAllRead(Long recipientId) {
        notifications.findByRecipientId(recipientId).forEach(n -> {
            n.markRead();
            notifications.save(n);
        });
    }
}
