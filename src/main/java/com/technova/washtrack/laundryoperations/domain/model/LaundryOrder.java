package com.technova.washtrack.laundryoperations.domain.model;

import java.time.Instant;

import com.technova.washtrack.shared.exception.BusinessRuleException;

/**
 * Aggregate root of the laundry operations context: tracks the internal reception and processing
 * stages of a confirmed order inside the laundry.
 */
public class LaundryOrder {

    private final Long id;
    private final Long orderId;
    private final String orderNumber;
    private final Long customerId;
    private final int garmentCount;
    private LaundryStatus status;
    private final Instant receivedAt;
    private Instant updatedAt;

    private LaundryOrder(Long id, Long orderId, String orderNumber, Long customerId, int garmentCount,
                         Instant now) {
        this.id = id;
        this.orderId = orderId;
        this.orderNumber = orderNumber.trim();
        this.customerId = customerId;
        this.garmentCount = garmentCount;
        this.status = LaundryStatus.RECEIVED;
        this.receivedAt = now;
        this.updatedAt = now;
    }

    /** Registers the reception of a confirmed order into the laundry; it starts in {@link LaundryStatus#RECEIVED}. */
    public static LaundryOrder receive(Long id, Long orderId, String orderNumber, Long customerId,
                                       int garmentCount, Instant now) {
        if (orderId == null || customerId == null || orderNumber == null || orderNumber.isBlank()) {
            throw new BusinessRuleException("Laundry order requires orderId, orderNumber and customerId");
        }
        if (garmentCount <= 0) {
            throw new BusinessRuleException("Laundry order must have at least one garment");
        }
        return new LaundryOrder(id, orderId, orderNumber, customerId, garmentCount, now);
    }

    /** Moves the order to the next processing stage; stages cannot be skipped or repeated. */
    public LaundryStatus advance(Instant now) {
        status = status.next().orElseThrow(
                () -> new BusinessRuleException("Laundry order " + orderNumber + " is already ready for delivery"));
        updatedAt = now;
        return status;
    }

    public boolean isReadyForDelivery() {
        return status == LaundryStatus.READY_FOR_DELIVERY;
    }

    public Long id() {
        return id;
    }

    public Long orderId() {
        return orderId;
    }

    public String orderNumber() {
        return orderNumber;
    }

    public Long customerId() {
        return customerId;
    }

    public int garmentCount() {
        return garmentCount;
    }

    public LaundryStatus status() {
        return status;
    }

    public Instant receivedAt() {
        return receivedAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
