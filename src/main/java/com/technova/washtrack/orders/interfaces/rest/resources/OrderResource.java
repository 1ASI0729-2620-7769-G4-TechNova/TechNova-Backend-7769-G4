package com.technova.washtrack.orders.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response resource representing an order.
 *
 */
@Schema(description = "Response resource representing an order")
public record OrderResource(
        @Schema(description = "Persistent identifier", example = "1")
        Long id,
        @Schema(description = "Unique business number of the order", example = "ORD-0001")
        String orderNumber,
        @Schema(description = "Identifier of the customer", example = "1")
        Long customerId,
        @Schema(description = "Identifier of the laundry", example = "1")
        Long laundryId,
        @Schema(description = "Current status of the order", example = "DRAFT")
        String status,
        @Schema(description = "Promised delivery date and time", example = "2026-12-01T18:00:00")
        LocalDateTime promisedDate,
        @Schema(description = "Total amount of the order", example = "45.50")
        BigDecimal totalAmount,
        @Schema(description = "ISO 4217 currency code", example = "PEN")
        String currency,
        @Schema(description = "Instructions from the customer", example = "Deliver after 6 pm")
        String specialInstructions,
        @Schema(description = "Reason of the cancellation", example = "The customer no longer needs the service")
        String cancellationReason,
        @Schema(description = "Items of the order")
        List<OrderItemResource> items,
        @Schema(description = "Creation date and time", example = "2026-11-16T15:30:00Z")
        Instant createdAt) {
}
