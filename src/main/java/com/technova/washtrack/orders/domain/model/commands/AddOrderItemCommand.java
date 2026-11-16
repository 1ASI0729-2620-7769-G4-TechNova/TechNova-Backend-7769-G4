package com.technova.washtrack.orders.domain.model.commands;

import com.technova.washtrack.orders.domain.model.valueobjects.GarmentType;
import com.technova.washtrack.orders.domain.model.valueobjects.Money;
import com.technova.washtrack.orders.domain.model.valueobjects.ServiceId;

import java.math.BigDecimal;

/**
 * Command for adding an item to an existing order that is still in {@code DRAFT} status.
 *
 * @param orderId     identifier of the order
 * @param serviceId   identifier of the requested laundry service
 * @param garmentType type of garment
 * @param quantity    number of garments
 * @param weightKg    optional weight of the garments in kilograms (informative)
 * @param unitPrice   price of one garment
 * @param notes       optional notes about the garments
 */
public record AddOrderItemCommand(
        Long orderId,
        ServiceId serviceId,
        GarmentType garmentType,
        Integer quantity,
        BigDecimal weightKg,
        Money unitPrice,
        String notes) {
    /**
     * Validates the command.
     * Quantity, weight, price and notes rules are enforced by the {@code OrderItem} entity.
     *
     * @throws IllegalArgumentException if {@code orderId} is null or not greater than 0,
     *                                  or if {@code serviceId}, {@code garmentType} or {@code unitPrice} is null
     */
    public AddOrderItemCommand {
        if (orderId == null || orderId <= 0)
            throw new IllegalArgumentException("order.error.id.invalid");
        if (serviceId == null)
            throw new IllegalArgumentException("order.error.item.serviceId.notNull");
        if (garmentType == null)
            throw new IllegalArgumentException("order.error.item.garmentType.notNull");
        if (unitPrice == null)
            throw new IllegalArgumentException("order.error.item.unitPrice.notNull");
    }
}