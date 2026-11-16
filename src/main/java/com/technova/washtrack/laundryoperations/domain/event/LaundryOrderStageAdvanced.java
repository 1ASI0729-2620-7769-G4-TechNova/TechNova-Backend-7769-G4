package com.technova.washtrack.laundryoperations.domain.event;

import com.technova.washtrack.laundryoperations.domain.model.LaundryStatus;

/** A laundry order moved to a new internal processing stage. */
public record LaundryOrderStageAdvanced(Long laundryOrderId, Long orderId, String orderNumber, Long customerId,
                                        LaundryStatus status) {
}
