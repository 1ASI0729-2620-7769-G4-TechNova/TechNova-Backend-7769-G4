package com.technova.washtrack.billing.interfaces.rest;

import java.math.BigDecimal;
import java.time.Instant;

import com.technova.washtrack.billing.domain.model.Payment;

public record PaymentResource(Long id, Long orderId, String customerName, BigDecimal amount,
        String paymentMethod, String status, String reference, Instant paidAt, String rejectionReason) {

    static PaymentResource from(Payment payment) {
        return new PaymentResource(payment.id(), payment.orderId(), payment.customerName(), payment.amount(),
                payment.method().name(), payment.status().name(), payment.reference(), payment.paidAt(),
                payment.rejectionReason());
    }
}
