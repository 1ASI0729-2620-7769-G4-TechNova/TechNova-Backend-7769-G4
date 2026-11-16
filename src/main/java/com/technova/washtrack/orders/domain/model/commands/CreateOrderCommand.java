package com.technova.washtrack.orders.domain.model.commands;

import com.technova.washtrack.orders.domain.model.valueobjects.CustomerId;
import com.technova.washtrack.orders.domain.model.valueobjects.LaundryId;
import com.technova.washtrack.orders.domain.model.valueobjects.PromisedDate;

import java.util.List;

/**
 * Command for creating a new order in {@code DRAFT} status.
 * The items list can be empty, because items can also be added later with {@link AddOrderItemCommand};
 * an order is only required to have items when it is confirmed.
 *
 * @param customerId          identifier of the customer who owns the order
 * @param laundryId           identifier of the laundry that receives the order
 * @param promisedDate        date and time the laundry commits to deliver the order
 * @param currency            ISO 4217 currency code used for every amount of the order
 * @param specialInstructions optional instructions from the customer
 * @param items               items registered together with the order
 */
public record CreateOrderCommand(
        CustomerId customerId,
        LaundryId laundryId,
        PromisedDate promisedDate,
        String currency,
        String specialInstructions,
        List<CreateOrderItemData> items) {
    /**
     * Validates the command.
     * Further validation (currency format, promised date in the future and instructions length)
     * is enforced by the {@code Order} aggregate.
     *
     * @throws IllegalArgumentException if {@code customerId}, {@code laundryId}, {@code promisedDate},
     *                                  {@code currency} or {@code items} is null, or if an item is null
     */
    public CreateOrderCommand {
        if (customerId == null)
            throw new IllegalArgumentException("order.error.customerId.notNull");
        if (laundryId == null)
            throw new IllegalArgumentException("order.error.laundryId.notNull");
        if (promisedDate == null)
            throw new IllegalArgumentException("order.error.promisedDate.notNull");
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("order.error.money.currency.notBlank");
        if (items == null)
            throw new IllegalArgumentException("order.error.items.notNull");
        for (CreateOrderItemData item : items) {
            if (item == null)
                throw new IllegalArgumentException("order.error.items.notNull");
        }
        items = List.copyOf(items);
    }
}