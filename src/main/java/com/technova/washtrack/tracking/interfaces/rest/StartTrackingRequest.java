package com.technova.washtrack.tracking.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StartTrackingRequest(
        @NotNull @Positive Long orderId,
        @NotBlank String orderNumber,
        @NotNull @Positive Long customerId) {
}
