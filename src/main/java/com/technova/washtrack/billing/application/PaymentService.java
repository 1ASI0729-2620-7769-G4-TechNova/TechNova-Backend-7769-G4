package com.technova.washtrack.billing.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.technova.washtrack.billing.domain.model.Payment;
import com.technova.washtrack.billing.domain.model.PaymentMethod;
import com.technova.washtrack.billing.domain.model.PaymentStatus;
import com.technova.washtrack.billing.domain.repository.PaymentRepository;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;

@Service
public class PaymentService {

    private final PaymentRepository payments;
    private final Clock clock;

    PaymentService(PaymentRepository payments, Clock clock) {
        this.payments = payments;
        this.clock = clock;
    }

    public Payment register(Long orderId, String customerName, BigDecimal amount, PaymentMethod method) {
        Payment payment = Payment.register(payments.nextId(), orderId, customerName, amount, method,
                clock.instant());
        return payments.save(payment);
    }

    public Payment getById(Long id) {
        return payments.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    public List<Payment> search(Long orderId, PaymentStatus status, Instant from, Instant to) {
        return payments.search(orderId, status, from, to);
    }
}
