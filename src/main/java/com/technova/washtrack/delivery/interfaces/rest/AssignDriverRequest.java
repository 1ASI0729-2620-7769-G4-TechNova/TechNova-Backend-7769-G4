package com.technova.washtrack.delivery.interfaces.rest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignDriverRequest(@NotNull @Positive Long driverId) {
}
