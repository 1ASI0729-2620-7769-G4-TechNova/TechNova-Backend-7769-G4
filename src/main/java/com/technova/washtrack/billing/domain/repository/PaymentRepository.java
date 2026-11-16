package com.technova.washtrack.billing.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.technova.washtrack.billing.domain.model.Payment;
import com.technova.washtrack.billing.domain.model.PaymentStatus;

public interface PaymentRepository {

    Long nextId();

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    /**
     * Searches payments, newest first. Every criterion is optional (null means "any");
     * {@code from} is inclusive and {@code to} is exclusive.
     */
    List<Payment> search(Long orderId, PaymentStatus status, Instant from, Instant to);
}
