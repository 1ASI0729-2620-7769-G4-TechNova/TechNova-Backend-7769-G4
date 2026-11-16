package com.technova.washtrack.delivery.domain.model;

import java.time.Instant;

import com.technova.washtrack.shared.exception.BusinessRuleException;

/** Aggregate that coordinates the pickup or home delivery of an order. */
public class Delivery {

    private final Long id;
    private final Long orderId;
    private final String customerName;
    private final DeliveryType type;
    private final String address;
    private final Instant scheduledAt;
    private DeliveryStatus status;
    private Long driverId;
    private String driverName;

    private Delivery(Long id, Long orderId, String customerName, DeliveryType type, String address,
            Instant scheduledAt) {
        this.id = id;
        this.orderId = orderId;
        this.customerName = customerName;
        this.type = type;
        this.address = address;
        this.scheduledAt = scheduledAt;
        this.status = DeliveryStatus.SCHEDULED;
    }

    public static Delivery schedule(Long id, Long orderId, String customerName, DeliveryType type,
            String address, Instant scheduledAt) {
        return new Delivery(id, orderId, customerName, type, address, scheduledAt);
    }

    /** Assigns the driver in charge; only possible while the delivery is still SCHEDULED. */
    public void assignDriver(Driver driver) {
        if (status != DeliveryStatus.SCHEDULED) {
            throw new BusinessRuleException("A driver can only be assigned to a SCHEDULED delivery");
        }
        if (!driver.isAvailable()) {
            throw new BusinessRuleException("Driver " + driver.name() + " is not available");
        }
        driverId = driver.id();
        driverName = driver.name();
    }

    public Long driverId() {
        return driverId;
    }

    public String driverName() {
        return driverName;
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

    public DeliveryType type() {
        return type;
    }

    public String address() {
        return address;
    }

    public Instant scheduledAt() {
        return scheduledAt;
    }

    public DeliveryStatus status() {
        return status;
    }
}
