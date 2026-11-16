package com.technova.washtrack.billing.domain.event;

/** A payment was refused; the order stays pending payment. */
public record PaymentRejected(Long paymentId, Long orderId, String reason) {
}
