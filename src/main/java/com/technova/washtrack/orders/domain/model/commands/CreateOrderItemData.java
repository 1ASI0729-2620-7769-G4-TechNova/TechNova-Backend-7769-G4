package com.technova.washtrack.orders.domain.model.commands;

import com.technova.washtrack.orders.domain.model.valueobjects.GarmentType;
import com.technova.washtrack.orders.domain.model.valueobjects.Money;
import com.technova.washtrack.orders.domain.model.valueobjects.ServiceId;

import java.math.BigDecimal;

/**
 * Data of an item registered together with a new order through {@link CreateOrderCommand}.
 * Quantity, weight, price and notes rules are enforced by the {@code OrderItem} entity.
 *
 * @param serviceId   identifier of the requested laundry service
 * @param garmentType type of garment
 * @param quantity    number of garments
 * @param weightKg    optional weight of the garments in kilograms (informative)
 * @param unitPrice   price of one garment
 * @param notes       optional notes about the garments
 */
public record CreateOrderItemData(
        ServiceId serviceId,
        GarmentType garmentType,
        Integer quantity,
        BigDecimal weightKg,
        Money unitPrice,
        String notes) {
    /**
     * Validates the item data.
     *
     * @throws IllegalArgumentException if {@code serviceId}, {@code garmentType} or {@code unitPrice} is null
     */
    public CreateOrderItemData {
        if (serviceId == null)
            throw new IllegalArgumentException("order.error.item.serviceId.notNull");
        if (garmentType == null)
            throw new IllegalArgumentException("order.error.item.garmentType.notNull");
        if (unitPrice == null)
            throw new IllegalArgumentException("order.error.item.unitPrice.notNull");
    }
}