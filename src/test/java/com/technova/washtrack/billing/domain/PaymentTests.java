package com.technova.washtrack.billing.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.technova.washtrack.billing.domain.model.Payment;
import com.technova.washtrack.billing.domain.model.PaymentMethod;
import com.technova.washtrack.billing.domain.model.PaymentStatus;
import com.technova.washtrack.shared.exception.BusinessRuleException;

class PaymentTests {

    private static Payment register(BigDecimal amount) {
        return Payment.register(7L, 101L, "Ana Martinez", amount, PaymentMethod.YAPE, Instant.parse("2026-09-02T10:15:00Z"));
    }

    @Test
    void registeredPaymentStartsPendingWithReference() {
        Payment payment = register(new BigDecimal("45.50"));

        assertEquals(PaymentStatus.PENDING, payment.status());
        assertEquals("PAY-0007", payment.reference());
    }

    @Test
    void pendingPaymentCanBeConfirmedOrRejectedOnlyOnce() {
        Payment confirmed = register(BigDecimal.TEN);
        confirmed.confirm();
        assertEquals(PaymentStatus.CONFIRMED, confirmed.status());
        assertThrows(BusinessRuleException.class, confirmed::confirm);
        assertThrows(BusinessRuleException.class, () -> confirmed.reject("late"));

        Payment rejected = register(BigDecimal.TEN);
        rejected.reject("Insufficient funds");
        assertEquals(PaymentStatus.REJECTED, rejected.status());
        assertEquals("Insufficient funds", rejected.rejectionReason());
        assertThrows(BusinessRuleException.class, rejected::confirm);
    }

    @Test
    void rejectsZeroNegativeOrMissingAmount() {
        assertThrows(BusinessRuleException.class, () -> register(BigDecimal.ZERO));
        assertThrows(BusinessRuleException.class, () -> register(new BigDecimal("-1")));
        assertThrows(BusinessRuleException.class, () -> register(null));
    }
}
