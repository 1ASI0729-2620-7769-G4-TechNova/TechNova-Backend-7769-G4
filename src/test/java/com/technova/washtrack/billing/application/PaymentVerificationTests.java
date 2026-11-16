package com.technova.washtrack.billing.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import com.technova.washtrack.billing.domain.event.PaymentConfirmed;
import com.technova.washtrack.billing.domain.event.PaymentRegistered;
import com.technova.washtrack.billing.domain.event.PaymentRejected;
import com.technova.washtrack.billing.domain.model.Payment;
import com.technova.washtrack.billing.domain.model.PaymentMethod;
import com.technova.washtrack.billing.domain.model.PaymentStatus;
import com.technova.washtrack.shared.exception.BusinessRuleException;

@SpringBootTest
@RecordApplicationEvents
@Import(PaymentVerificationTests.StrictGateway.class)
class PaymentVerificationTests {

    /** Provider stub that refuses anything above S/ 1000. */
    @TestConfiguration
    static class StrictGateway {

        @Bean
        @Primary
        PaymentGateway gateway() {
            return payment -> payment.amount().compareTo(BigDecimal.valueOf(1000)) > 0
                    ? PaymentGateway.Verification.refuse("Insufficient funds")
                    : PaymentGateway.Verification.approve();
        }
    }

    @Autowired
    private PaymentService payments;

    @Autowired
    private ApplicationEvents events;

    private Payment register(String amount, PaymentMethod method) {
        return payments.register(301L, "Ana Martinez", new BigDecimal(amount), method);
    }

    @Test
    void approvedDigitalPaymentIsConfirmed() {
        Payment payment = register("50", PaymentMethod.CARD);

        assertEquals(PaymentStatus.CONFIRMED, payment.status());
        assertEquals(1, events.stream(PaymentRegistered.class).count());
        assertEquals(1, events.stream(PaymentConfirmed.class).count());
    }

    @Test
    void refusedDigitalPaymentIsRejectedWithReason() {
        Payment payment = register("2000", PaymentMethod.YAPE);

        assertEquals(PaymentStatus.REJECTED, payment.status());
        assertEquals("Insufficient funds", payment.rejectionReason());
        assertEquals(1, events.stream(PaymentRejected.class).count());
    }

    @Test
    void cashPaymentWaitsForManualConfirmation() {
        Payment payment = register("2000", PaymentMethod.CASH);
        assertEquals(PaymentStatus.PENDING, payment.status());

        payments.confirm(payment.id());

        assertEquals(PaymentStatus.CONFIRMED, payments.getById(payment.id()).status());
        assertThrows(BusinessRuleException.class, () -> payments.reject(payment.id(), "too late"));
    }
}
