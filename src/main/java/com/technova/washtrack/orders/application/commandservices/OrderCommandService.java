package com.technova.washtrack.orders.application.commandservices;

import com.technova.washtrack.orders.domain.model.aggregates.Order;
import com.technova.washtrack.orders.domain.model.commands.AddOrderItemCommand;
import com.technova.washtrack.orders.domain.model.commands.CancelOrderCommand;
import com.technova.washtrack.orders.domain.model.commands.ConfirmOrderCommand;
import com.technova.washtrack.orders.domain.model.commands.CreateOrderCommand;
import com.technova.washtrack.shared.application.result.Result;

/**
 * Application service contract for order command operations.
 * Every command returns a Result that distinguishes a successful operation from
 * a business failure described by {@link OrderCommandFailure}.
 *
 */
public interface OrderCommandService {
    /**
     * Handles the creation of an order in DRAFT status with a unique order number.
     *
     * @param command create command containing the customer, laundry, promised date, currency, instructions and items
     * @return success with the created order
     * @throws IllegalArgumentException if the promised date has already passed or an item is invalid
     * @see CreateOrderCommand
     */
    Result<Order, OrderCommandFailure> handle(CreateOrderCommand command);

    /**
     * Handles adding an item to an order and recalculating its total amount.
     *
     * @param command add item command containing the order identifier and the item data
     * @return success with the updated order, or failure when the order does not exist or is not in DRAFT status
     * @throws IllegalArgumentException if the item is invalid or uses a different currency
     * @see AddOrderItemCommand
     */
    Result<Order, OrderCommandFailure> handle(AddOrderItemCommand command);

    /**
     * Handles the confirmation of an order.
     *
     * @param command confirm command containing the order identifier
     * @return success with the confirmed order, or failure when the order does not exist,
     *         is not in DRAFT status or has no items
     * @see ConfirmOrderCommand
     */
    Result<Order, OrderCommandFailure> handle(ConfirmOrderCommand command);

    /**
     * Handles the cancellation of an order.
     *
     * @param command cancel command containing the order identifier and the reason
     * @return success with the cancelled order, or failure when the order does not exist
     *         or is not in DRAFT or CONFIRMED status
     * @throws IllegalArgumentException if the reason is too long
     * @see CancelOrderCommand
     */
    Result<Order, OrderCommandFailure> handle(CancelOrderCommand command);
}
