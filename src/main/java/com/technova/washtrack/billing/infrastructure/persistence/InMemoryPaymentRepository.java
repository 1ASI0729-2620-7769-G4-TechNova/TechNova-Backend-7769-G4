package com.technova.washtrack.billing.infrastructure.persistence;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.technova.washtrack.billing.domain.model.Payment;
import com.technova.washtrack.billing.domain.model.PaymentStatus;
import com.technova.washtrack.billing.domain.repository.PaymentRepository;

/** Volatile storage used until the database is connected; replaced by a JPA adapter. */
@Repository
class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<Long, Payment> payments = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Long nextId() {
        return sequence.incrementAndGet();
    }

    @Override
    public Payment save(Payment payment) {
        payments.put(payment.id(), payment);
        return payment;
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return Optional.ofNullable(payments.get(id));
    }

    @Override
    public List<Payment> search(Long orderId, PaymentStatus status, Instant from, Instant to) {
        return payments.values().stream()
                .filter(p -> orderId == null || p.orderId().equals(orderId))
                .filter(p -> status == null || p.status() == status)
                .filter(p -> from == null || !p.paidAt().isBefore(from))
                .filter(p -> to == null || p.paidAt().isBefore(to))
                .sorted(Comparator.comparing(Payment::paidAt).reversed())
                .toList();
    }
}
