package com.technova.washtrack.billing.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

import com.technova.washtrack.shared.exception.BusinessRuleException;

/** Aggregate that records the amount, method and outcome of a transaction for a laundry order. */
public class Payment {

    private final Long id;
    private final Long orderId;
    private final String customerName;
    private final BigDecimal amount;
    private final PaymentMethod method;
    private final Instant paidAt;
    private PaymentStatus status;

    private Payment(Long id, Long orderId, String customerName, BigDecimal amount, PaymentMethod method,
            Instant paidAt) {
        this.id = id;
        this.orderId = orderId;
        this.customerName = customerName;
        this.amount = amount;
        this.method = method;
        this.paidAt = paidAt;
        this.status = PaymentStatus.PENDING;
    }

    /** Registers a new payment, which starts as {@link PaymentStatus#PENDING}. */
    public static Payment register(Long id, Long orderId, String customerName, BigDecimal amount,
            PaymentMethod method, Instant paidAt) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessRuleException("Payment amount must be greater than zero");
        }
        return new Payment(id, orderId, customerName, amount, method, paidAt);
    }

    public String reference() {
        return "PAY-%04d".formatted(id);
    }

    public Long id() {
        return id;
    }

    public Long orderId() {
        return orderId;
    }

    public String customerName() {
        return customerName;
    }

    public BigDecimal amount() {
        return amount;
    }

    public PaymentMethod method() {
        return method;
    }

    public Instant paidAt() {
        return paidAt;
    }

    public PaymentStatus status() {
        return status;
    }
}
