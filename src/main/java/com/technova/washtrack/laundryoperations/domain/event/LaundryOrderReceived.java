package com.technova.washtrack.laundryoperations.domain.event;

/** A confirmed order was physically received at the laundry. */
public record LaundryOrderReceived(Long laundryOrderId, Long orderId, String orderNumber, Long customerId,
                                   int garmentCount) {
}
