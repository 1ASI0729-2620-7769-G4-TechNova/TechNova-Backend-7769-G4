package com.technova.washtrack.tracking.interfaces.rest;

import com.technova.washtrack.tracking.domain.model.OrderStage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateNotificationRequest(
        @NotNull @Positive Long recipientId,
        @NotNull @Positive Long orderId,
        @NotBlank String orderNumber,
        @NotNull OrderStage stage) {
}
