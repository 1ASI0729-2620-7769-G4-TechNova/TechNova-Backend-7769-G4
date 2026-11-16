package com.technova.washtrack.orders.domain.model.commands;

/**
 * Command for confirming an order that is in {@code DRAFT} status.
 *
 * @param orderId identifier of the order
 *
 */
public record ConfirmOrderCommand(Long orderId) {
    /**
     * Validates the command.
     *
     * @throws IllegalArgumentException if {@code orderId} is null or not greater than 0
     */
    public ConfirmOrderCommand {
        if (orderId == null || orderId <= 0)
            throw new IllegalArgumentException("order.error.id.invalid");
    }
}