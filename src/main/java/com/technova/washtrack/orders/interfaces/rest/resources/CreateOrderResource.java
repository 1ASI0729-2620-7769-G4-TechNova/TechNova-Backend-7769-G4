package com.technova.washtrack.orders.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request resource, used to create an order.
 *
 */
@Schema(description = "Request resource used to create an order")
public record CreateOrderResource(
        @Schema(description = "Identifier of the customer", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{order.error.customerId.notNull}")
        @Positive(message = "{order.error.customerId.invalid}")
        Long customerId,

        @Schema(description = "Identifier of the laundry", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{order.error.laundryId.notNull}")
        @Positive(message = "{order.error.laundryId.invalid}")
        Long laundryId,

        @Schema(description = "Promised delivery date and time", example = "2026-12-01T18:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{order.error.promisedDate.notNull}")
        @Future(message = "{order.error.promisedDate.past}")
        LocalDateTime promisedDate,

        @Schema(description = "ISO 4217 currency code", example = "PEN", pattern = "^[A-Za-z]{3}$", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{order.error.money.currency.notBlank}")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "{order.error.money.currency.pattern}")
        String currency,

        @Schema(description = "Optional instructions from the customer", example = "Deliver after 6 pm", maxLength = 500)
        @Size(max = 500, message = "{order.error.specialInstructions.size}")
        String specialInstructions,

        @Schema(description = "Items registered together with the order", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{order.error.items.notNull}")
        List<@NotNull(message = "{order.error.items.notNull}") @Valid OrderItemResource> items) {
}
