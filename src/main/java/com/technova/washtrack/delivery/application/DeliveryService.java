package com.technova.washtrack.delivery.application;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.technova.washtrack.delivery.domain.event.DeliveryCompleted;
import com.technova.washtrack.delivery.domain.event.DeliveryFailed;
import com.technova.washtrack.delivery.domain.model.Delivery;
import com.technova.washtrack.delivery.domain.model.DeliveryStatus;
import com.technova.washtrack.delivery.domain.model.DeliveryType;
import com.technova.washtrack.delivery.domain.model.Driver;
import com.technova.washtrack.delivery.domain.repository.DeliveryRepository;
import com.technova.washtrack.shared.exception.BusinessRuleException;
import com.technova.washtrack.shared.exception.ConflictException;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveries;
    private final Clock clock;
    private final int slotCapacity;
    private final DriverService drivers;
    private final ApplicationEventPublisher events;

    DeliveryService(DeliveryRepository deliveries, Clock clock, DriverService drivers,
            ApplicationEventPublisher events, @Value("${washtrack.delivery.slot-capacity:3}") int slotCapacity) {
        this.deliveries = deliveries;
        this.clock = clock;
        this.drivers = drivers;
        this.events = events;
        this.slotCapacity = slotCapacity;
    }

    public Delivery start(Long id) {
        Delivery delivery = getById(id);
        delivery.start();
        return deliveries.save(delivery);
    }

    public Delivery complete(Long id) {
        Delivery delivery = getById(id);
        delivery.complete(clock.instant());
        deliveries.save(delivery);
        events.publishEvent(new DeliveryCompleted(id, delivery.orderId(), delivery.type()));
        return delivery;
    }

    public Delivery fail(Long id, String reason) {
        Delivery delivery = getById(id);
        delivery.fail(reason);
        deliveries.save(delivery);
        events.publishEvent(new DeliveryFailed(id, delivery.orderId(), delivery.type(), reason));
        return delivery;
    }

    /** Assigns an available driver, unless that driver already has another active delivery in the same hour. */
    public Delivery assignDriver(Long deliveryId, Long driverId) {
        Delivery delivery = getById(deliveryId);
        Driver driver = drivers.getById(driverId);
        Instant slotStart = delivery.scheduledAt().truncatedTo(ChronoUnit.HOURS);
        boolean busy = deliveries.search(null, null, null, slotStart, slotStart.plus(1, ChronoUnit.HOURS)).stream()
                .anyMatch(d -> !d.id().equals(deliveryId) && d.status().isActive() && driverId.equals(d.driverId()));
        if (busy) {
            throw new ConflictException("Driver " + driver.name() + " already has a delivery in that time slot");
        }
        delivery.assignDriver(driver);
        return deliveries.save(delivery);
    }

    /** Schedules a pickup or delivery in a one-hour slot that still has capacity. */
    public Delivery schedule(Long orderId, String customerName, DeliveryType type, String address,
            Instant scheduledAt) {
        if (!scheduledAt.isAfter(clock.instant())) {
            throw new BusinessRuleException("A " + type + " must be scheduled in the future");
        }
        if (deliveries.search(orderId, null, type, null, null).stream().anyMatch(d -> d.status().isActive())) {
            throw new BusinessRuleException("Order " + orderId + " already has an active " + type);
        }
        Instant slotStart = scheduledAt.truncatedTo(ChronoUnit.HOURS);
        long taken = deliveries.search(null, null, null, slotStart, slotStart.plus(1, ChronoUnit.HOURS)).stream()
                .filter(d -> d.status().isActive()).count();
        if (taken >= slotCapacity) {
            throw new ConflictException("The requested time slot has no capacity left");
        }
        return deliveries.save(Delivery.schedule(deliveries.nextId(), orderId, customerName, type, address,
                scheduledAt));
    }

    public Delivery getById(Long id) {
        return deliveries.findById(id).orElseThrow(() -> new ResourceNotFoundException("Delivery", id));
    }

    public List<Delivery> search(Long orderId, DeliveryStatus status, DeliveryType type, Instant from, Instant to) {
        return deliveries.search(orderId, status, type, from, to);
    }
}
