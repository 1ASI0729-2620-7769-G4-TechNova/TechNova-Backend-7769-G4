package com.technova.washtrack.tracking.domain.repository;

import java.util.List;
import java.util.Optional;

import com.technova.washtrack.tracking.domain.model.Notification;

public interface NotificationRepository {

    Long nextId();

    Notification save(Notification notification);

    Optional<Notification> findById(Long id);

    /** Notifications of a recipient, newest first. */
    List<Notification> findByRecipientId(Long recipientId);
}
