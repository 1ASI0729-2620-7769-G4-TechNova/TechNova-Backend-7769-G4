package com.technova.washtrack.orders.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request resource, used to add an item to an order in DRAFT status.
 *
 */
@Schema(description = "Request resource used to add an item to an order")
public record AddOrderItemResource(
        @Schema(description = "Identifier of the requested laundry service", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{order.error.item.serviceId.notNull}")
        @Positive(message = "{order.error.serviceId.invalid}")
        Long serviceId,

        @Schema(description = "Type of garment", example = "PANTS",
                allowableValues = {"SHIRT", "PANTS", "SUIT", "BEDDING", "DELICATE", "BULK_LAUNDRY"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{order.error.item.garmentType.notNull}")
        String garmentType,

        @Schema(description = "Number of garments", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{order.error.item.quantity.invalid}")
        @Positive(message = "{order.error.item.quantity.invalid}")
        Integer quantity,

        @Schema(description = "Optional weight in kilograms (informative)", example = "1.20")
        @Positive(message = "{order.error.item.weightKg.invalid}")
        @Digits(integer = 6, fraction = 2, message = "{order.error.item.weightKg.digits}")
        BigDecimal weightKg,

        @Schema(description = "Price of one garment", example = "10.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{order.error.item.unitPrice.notNull}")
        @Positive(message = "{order.error.item.unitPrice.invalid}")
        @Digits(integer = 8, fraction = 2, message = "{order.error.item.unitPrice.digits}")
        BigDecimal unitPrice,

        @Schema(description = "ISO 4217 currency code of the unit price (same as the order)", example = "PEN",
                pattern = "^[A-Za-z]{3}$", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{order.error.money.currency.notBlank}")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "{order.error.money.currency.pattern}")
        String currency,

        @Schema(description = "Optional notes about the garments", example = "Dark blue jeans", maxLength = 255)
        @Size(max = 255, message = "{order.error.item.notes.size}")
        String notes) {
}
