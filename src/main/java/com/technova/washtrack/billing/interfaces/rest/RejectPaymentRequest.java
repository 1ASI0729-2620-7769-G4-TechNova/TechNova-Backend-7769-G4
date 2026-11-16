package com.technova.washtrack.billing.interfaces.rest;

import jakarta.validation.constraints.NotBlank;

public record RejectPaymentRequest(@NotBlank String reason) {
}
