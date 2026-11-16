package com.technova.washtrack.billing.interfaces.rest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SubscribeRequest(@NotNull @Positive Long laundryId, @NotNull @Positive Long planId) {
}
