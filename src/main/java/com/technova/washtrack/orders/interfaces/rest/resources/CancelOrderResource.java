package com.technova.washtrack.orders.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request resource, used to cancel an order.
 *
 */
@Schema(description = "Request resource used to cancel an order")
public record CancelOrderResource(
        @Schema(description = "Reason for the cancellation", example = "The customer no longer needs the service",
                maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{order.error.cancellationReason.notBlank}")
        @Size(max = 255, message = "{order.error.cancellationReason.size}")
        String reason) {
}
