package com.technova.washtrack.orders.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Resource representing an item of an order.
 */
@Schema(description = "Resource representing an item of an order")
public record OrderItemResource(
        @Schema(description = "Persistent identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @Schema(description = "Identifier of the requested laundry service", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{order.error.item.serviceId.notNull}")
        @Positive(message = "{order.error.serviceId.invalid}")
        Long serviceId,

        @Schema(description = "Type of garment", example = "SHIRT",
                allowableValues = {"SHIRT", "PANTS", "SUIT", "BEDDING", "DELICATE", "BULK_LAUNDRY"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{order.error.item.garmentType.notNull}")
        String garmentType,

        @Schema(description = "Number of garments", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{order.error.item.quantity.invalid}")
        @Positive(message = "{order.error.item.quantity.invalid}")
        Integer quantity,

        @Schema(description = "Optional weight in kilograms (informative)", example = "1.50")
        @Positive(message = "{order.error.item.weightKg.invalid}")
        @Digits(integer = 6, fraction = 2, message = "{order.error.item.weightKg.digits}")
        BigDecimal weightKg,

        @Schema(description = "Price of one garment", example = "8.50", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{order.error.item.unitPrice.notNull}")
        @Positive(message = "{order.error.item.unitPrice.invalid}")
        @Digits(integer = 8, fraction = 2, message = "{order.error.item.unitPrice.digits}")
        BigDecimal unitPrice,

        @Schema(description = "Calculated subtotal (unitPrice x quantity)", example = "25.50", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal subtotal,

        @Schema(description = "Optional notes about the garments", example = "White shirts, no starch", maxLength = 255)
        @Size(max = 255, message = "{order.error.item.notes.size}")
        String notes) {
}
