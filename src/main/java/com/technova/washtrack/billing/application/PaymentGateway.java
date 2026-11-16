package com.technova.washtrack.billing.application;

import com.technova.washtrack.billing.domain.model.Payment;

/** Port to the external payment provider that authorises or refuses digital payments. */
public interface PaymentGateway {

    Verification verify(Payment payment);

    record Verification(boolean approved, String reason) {

        public static Verification approve() {
            return new Verification(true, null);
        }

        public static Verification refuse(String reason) {
            return new Verification(false, reason);
        }
    }
}
