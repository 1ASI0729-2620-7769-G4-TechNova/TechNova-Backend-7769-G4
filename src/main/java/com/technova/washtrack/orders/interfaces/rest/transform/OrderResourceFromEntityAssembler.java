package com.technova.washtrack.orders.interfaces.rest.transform;

import com.technova.washtrack.orders.domain.model.aggregates.Order;
import com.technova.washtrack.orders.domain.model.entities.OrderItem;
import com.technova.washtrack.orders.interfaces.rest.resources.OrderItemResource;
import com.technova.washtrack.orders.interfaces.rest.resources.OrderResource;

/**
 * Interface layer translator converting domain aggregates to HTTP outbound Resources.
 */
public class OrderResourceFromEntityAssembler {
    /**
     * Converts an Order aggregate to an OrderResource.
     */
    public static OrderResource toResourceFromEntity(Order entity) {
        var items = entity.getItems().stream()
                .map(OrderResourceFromEntityAssembler::toItemResourceFromEntity)
                .toList();
        return new OrderResource(
                entity.getId(),
                entity.getOrderNumber().value(),
                entity.getCustomerId().value(),
                entity.getLaundryId().value(),
                entity.getStatus().name(),
                entity.getPromisedDate().value(),
                entity.getTotalAmount().amount(),
                entity.getTotalAmount().currency(),
                entity.getSpecialInstructions(),
                entity.getCancellationReason(),
                items,
                entity.getCreatedAt());
    }

    /**
     * Converts an OrderItem entity to an OrderItemResource.
     */
    public static OrderItemResource toItemResourceFromEntity(OrderItem entity) {
        return new OrderItemResource(
                entity.getId(),
                entity.getServiceId().value(),
                entity.getGarmentType().name(),
                entity.getQuantity(),
                entity.getWeightKg(),
                entity.getUnitPrice().amount(),
                entity.getSubtotal().amount(),
                entity.getNotes());
    }
}
