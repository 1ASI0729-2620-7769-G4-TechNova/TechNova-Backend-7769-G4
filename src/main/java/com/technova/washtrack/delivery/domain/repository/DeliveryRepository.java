package com.technova.washtrack.delivery.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.technova.washtrack.delivery.domain.model.Delivery;
import com.technova.washtrack.delivery.domain.model.DeliveryStatus;
import com.technova.washtrack.delivery.domain.model.DeliveryType;

public interface DeliveryRepository {

    Long nextId();

    Delivery save(Delivery delivery);

    Optional<Delivery> findById(Long id);

    /** Searches deliveries by scheduled time, soonest first. Null criteria mean "any"; {@code to} is exclusive. */
    List<Delivery> search(Long orderId, DeliveryStatus status, DeliveryType type, Instant from, Instant to);
}
