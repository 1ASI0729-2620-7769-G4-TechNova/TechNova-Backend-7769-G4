package com.technova.washtrack.orders.domain.model.commands;

/**
 * Command for cancelling an order that has not entered processing yet.
 *
 * @param orderId identifier of the order
 * @param reason  reason for the cancellation
 *
 */
public record CancelOrderCommand(Long orderId, String reason) {
    /**
     * Validates the command.
     * The maximum length of the reason is enforced by the {@code Order} aggregate.
     *
     * @throws IllegalArgumentException if {@code orderId} is null or not greater than 0, or if {@code reason} is blank
     */
    public CancelOrderCommand {
        if (orderId == null || orderId <= 0)
            throw new IllegalArgumentException("order.error.id.invalid");
        if (reason == null || reason.isBlank())
            throw new IllegalArgumentException("order.error.cancellationReason.notBlank");
    }
}