package com.technova.washtrack.orders.interfaces.rest.transform;

import com.technova.washtrack.orders.domain.model.commands.CreateOrderCommand;
import com.technova.washtrack.orders.domain.model.commands.CreateOrderItemData;
import com.technova.washtrack.orders.interfaces.rest.resources.CreateOrderResource;
import com.technova.washtrack.orders.interfaces.rest.resources.OrderItemResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface layer translator converting HTTP inbound Resources to domain commands.
 */
public class CreateOrderCommandFromResourceAssembler {
    /**
     * Converts a CreateOrderResource to a CreateOrderCommand.
     * Every item price uses the currency of the order.
     */
    public static CreateOrderCommand toCommandFromResource(CreateOrderResource resource) {
        List<CreateOrderItemData> items = null;
        if (resource.items() != null) {
            items = new ArrayList<>();
            for (OrderItemResource item : resource.items()) {
                items.add(toItemDataFromResource(item, resource.currency()));
            }
        }
        return new CreateOrderCommand(
                OrderValueObjectFromPrimitiveAssembler.toCustomerId(resource.customerId()),
                OrderValueObjectFromPrimitiveAssembler.toLaundryId(resource.laundryId()),
                OrderValueObjectFromPrimitiveAssembler.toPromisedDate(resource.promisedDate()),
                resource.currency(),
                resource.specialInstructions(),
                items);
    }

    /**
     * Converts an OrderItemResource to the item data of a create order command.
     */
    private static CreateOrderItemData toItemDataFromResource(OrderItemResource item, String currency) {
        return new CreateOrderItemData(
                OrderValueObjectFromPrimitiveAssembler.toServiceId(item.serviceId()),
                OrderValueObjectFromPrimitiveAssembler.toGarmentType(item.garmentType()),
                item.quantity(),
                item.weightKg(),
                OrderValueObjectFromPrimitiveAssembler.toMoney(item.unitPrice(), currency),
                item.notes());
    }
}
