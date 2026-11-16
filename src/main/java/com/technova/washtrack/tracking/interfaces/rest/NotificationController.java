package com.technova.washtrack.tracking.interfaces.rest;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technova.washtrack.tracking.application.NotificationService;
import com.technova.washtrack.tracking.domain.model.Notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Order status messages for customers")
public class NotificationController {

    private final NotificationService notifications;

    NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @PostMapping
    @Operation(summary = "Create a notification manually (the system creates one on every stage change)")
    ResponseEntity<NotificationResource> create(@Valid @RequestBody CreateNotificationRequest request) {
        Notification created = notifications.create(request.recipientId(), request.orderId(), request.orderNumber(),
                request.stage());
        return ResponseEntity.created(URI.create("/api/v1/notifications/" + created.id()))
                .body(NotificationResource.from(created));
    }

    @GetMapping
    @Operation(summary = "Notifications of a recipient, newest first; unread=true hides the read ones")
    List<NotificationResource> list(@RequestParam Long recipientId, @RequestParam(defaultValue = "false") boolean unread) {
        return notifications.list(recipientId, unread).stream().map(NotificationResource::from).toList();
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    NotificationResource markRead(@PathVariable Long id) {
        return NotificationResource.from(notifications.markRead(id));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark every notification of a recipient as read")
    ResponseEntity<Void> markAllRead(@RequestParam Long recipientId) {
        notifications.markAllRead(recipientId);
        return ResponseEntity.noContent().build();
    }
}
