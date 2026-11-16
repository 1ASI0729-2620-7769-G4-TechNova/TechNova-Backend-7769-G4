package com.technova.washtrack.delivery.application;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.technova.washtrack.delivery.domain.model.Delivery;
import com.technova.washtrack.delivery.domain.model.DeliveryStatus;
import com.technova.washtrack.delivery.domain.model.DeliveryType;
import com.technova.washtrack.delivery.domain.repository.DeliveryRepository;
import com.technova.washtrack.shared.exception.BusinessRuleException;
import com.technova.washtrack.shared.exception.ConflictException;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveries;
    private final Clock clock;
    private final int slotCapacity;

    DeliveryService(DeliveryRepository deliveries, Clock clock,
            @Value("${washtrack.delivery.slot-capacity:3}") int slotCapacity) {
        this.deliveries = deliveries;
        this.clock = clock;
        this.slotCapacity = slotCapacity;
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
