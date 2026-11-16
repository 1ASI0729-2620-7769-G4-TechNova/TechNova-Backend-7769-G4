package com.technova.washtrack.orders.interfaces.rest.transform;

import com.technova.washtrack.orders.domain.model.commands.AddOrderItemCommand;
import com.technova.washtrack.orders.interfaces.rest.resources.AddOrderItemResource;

/**
 * Interface layer translator converting HTTP inbound Resources to domain commands.
 */
public class AddOrderItemCommandFromResourceAssembler {
    /**
     * Converts an AddOrderItemResource to an AddOrderItemCommand.
     */
    public static AddOrderItemCommand toCommandFromResource(Long orderId, AddOrderItemResource resource) {
        return new AddOrderItemCommand(
                orderId,
                OrderValueObjectFromPrimitiveAssembler.toServiceId(resource.serviceId()),
                OrderValueObjectFromPrimitiveAssembler.toGarmentType(resource.garmentType()),
                resource.quantity(),
                resource.weightKg(),
                OrderValueObjectFromPrimitiveAssembler.toMoney(resource.unitPrice(), resource.currency()),
                resource.notes());
    }
}
