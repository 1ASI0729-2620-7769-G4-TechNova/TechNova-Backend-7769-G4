package com.technova.washtrack.billing.infrastructure.gateway;

import org.springframework.stereotype.Component;

import com.technova.washtrack.billing.application.PaymentGateway;
import com.technova.washtrack.billing.domain.model.Payment;

/** Stand-in for the real provider: approves every payment. Swap for a real adapter when one is chosen. */
@Component
class SimulatedPaymentGateway implements PaymentGateway {

    @Override
    public Verification verify(Payment payment) {
        return Verification.approve();
    }
}
