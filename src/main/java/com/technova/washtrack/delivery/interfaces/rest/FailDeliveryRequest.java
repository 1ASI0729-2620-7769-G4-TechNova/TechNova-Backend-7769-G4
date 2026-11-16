package com.technova.washtrack.delivery.interfaces.rest;

import jakarta.validation.constraints.NotBlank;

public record FailDeliveryRequest(@NotBlank String reason) {
}
