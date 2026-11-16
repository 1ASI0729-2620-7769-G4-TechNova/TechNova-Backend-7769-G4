package com.technova.washtrack.laundryoperations.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReceiveLaundryOrderRequest(
        @NotNull @Positive Long orderId,
        @NotBlank String orderNumber,
        @NotNull @Positive Long customerId,
        @NotNull @Positive Integer garmentCount) {
}