package com.technova.washtrack.delivery.interfaces.rest;

import java.time.Instant;

import com.technova.washtrack.delivery.domain.model.DeliveryType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ScheduleDeliveryRequest(
        @NotNull @Positive Long orderId,
        @NotBlank String customerName,
        @NotNull DeliveryType type,
        @NotBlank String address,
        @NotNull Instant scheduledAt) {
}
