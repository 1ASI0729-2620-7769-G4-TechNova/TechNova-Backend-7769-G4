package com.technova.washtrack.billing.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.technova.washtrack.billing.domain.event.PaymentConfirmed;
import com.technova.washtrack.billing.domain.event.PaymentRegistered;
import com.technova.washtrack.billing.domain.event.PaymentRejected;
import com.technova.washtrack.billing.domain.model.Payment;
import com.technova.washtrack.billing.domain.model.PaymentMethod;
import com.technova.washtrack.billing.domain.model.PaymentStatus;
import com.technova.washtrack.billing.domain.repository.PaymentRepository;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;

@Service
public class PaymentService {

    private final PaymentRepository payments;
    private final Clock clock;
    private final ApplicationEventPublisher events;

    PaymentService(PaymentRepository payments, Clock clock, ApplicationEventPublisher events) {
        this.payments = payments;
        this.clock = clock;
        this.events = events;
    }

    public Payment register(Long orderId, String customerName, BigDecimal amount, PaymentMethod method) {
        Payment payment = payments.save(Payment.register(payments.nextId(), orderId, customerName, amount,
                method, clock.instant()));
        events.publishEvent(new PaymentRegistered(payment));
        return payment;
    }

    public Payment confirm(Long id) {
        Payment payment = getById(id);
        payment.confirm();
        payments.save(payment);
        events.publishEvent(new PaymentConfirmed(payment.id(), payment.orderId()));
        return payment;
    }

    public Payment reject(Long id, String reason) {
        Payment payment = getById(id);
        payment.reject(reason);
        payments.save(payment);
        events.publishEvent(new PaymentRejected(payment.id(), payment.orderId(), reason));
        return payment;
    }

    public Payment getById(Long id) {
        return payments.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    public List<Payment> search(Long orderId, PaymentStatus status, Instant from, Instant to) {
        return payments.search(orderId, status, from, to);
    }
}
