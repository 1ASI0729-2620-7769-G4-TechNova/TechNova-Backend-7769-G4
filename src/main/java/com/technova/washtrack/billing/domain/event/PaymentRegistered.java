package com.technova.washtrack.billing.domain.event;

import com.technova.washtrack.billing.domain.model.Payment;

/** A payment was recorded and is waiting for verification. */
public record PaymentRegistered(Payment payment) {
}
