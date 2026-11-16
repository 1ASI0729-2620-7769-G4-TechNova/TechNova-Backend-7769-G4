package com.technova.washtrack.billing.application;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.technova.washtrack.billing.domain.event.PaymentRegistered;
import com.technova.washtrack.billing.domain.model.Payment;
import com.technova.washtrack.billing.domain.model.PaymentMethod;

/**
 * Policy "when PaymentRegistered, verify the transaction with the provider".
 * Cash payments have no provider, so they wait for a manual confirmation.
 */
@Component
class PaymentVerificationPolicy {

    private final PaymentGateway gateway;
    private final PaymentService payments;

    PaymentVerificationPolicy(PaymentGateway gateway, PaymentService payments) {
        this.gateway = gateway;
        this.payments = payments;
    }

    @EventListener
    void onPaymentRegistered(PaymentRegistered event) {
        Payment payment = event.payment();
        if (payment.method() == PaymentMethod.CASH) {
            return;
        }
        PaymentGateway.Verification result = gateway.verify(payment);
        if (result.approved()) {
            payments.confirm(payment.id());
        } else {
            payments.reject(payment.id(), result.reason());
        }
    }
}
