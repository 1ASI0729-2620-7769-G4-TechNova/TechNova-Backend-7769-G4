package com.technova.washtrack.customer.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddAddressRequest(
        @NotBlank @Size(max = 150) String street,
        @NotBlank @Size(max = 60) String district,
        @Size(max = 150) String reference,
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "must be 9 to 15 digits") String contactPhone) {
}
