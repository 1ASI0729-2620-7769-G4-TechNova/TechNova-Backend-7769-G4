package com.technova.washtrack.billing.interfaces.rest;

import java.math.BigDecimal;

import com.technova.washtrack.billing.domain.model.PaymentMethod;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegisterPaymentRequest(
        @NotNull @Positive Long orderId,
        @NotBlank String customerName,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal amount,
        @NotNull PaymentMethod paymentMethod) {
}
