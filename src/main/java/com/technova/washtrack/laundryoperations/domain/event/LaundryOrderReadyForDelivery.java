package com.technova.washtrack.laundryoperations.domain.event;

/** All garments of the order have finished processing and are ready for pickup or delivery. */
public record LaundryOrderReadyForDelivery(Long laundryOrderId, Long orderId, String orderNumber, Long customerId) {
}