package com.technova.washtrack.billing.domain.event;

/** A payment was verified; the order can be marked as paid. */
public record PaymentConfirmed(Long paymentId, Long orderId) {
}
