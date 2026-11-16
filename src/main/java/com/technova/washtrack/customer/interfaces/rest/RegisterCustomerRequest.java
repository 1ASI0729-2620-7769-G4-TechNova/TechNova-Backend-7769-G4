package com.technova.washtrack.customer.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RegisterCustomerRequest(
        @NotNull @Positive Long userId,
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "must be 9 to 15 digits") String phone) {
}
