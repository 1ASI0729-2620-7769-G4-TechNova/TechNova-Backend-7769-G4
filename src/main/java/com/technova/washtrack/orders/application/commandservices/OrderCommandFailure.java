package com.technova.washtrack.orders.application.commandservices;

/**
 * Failure types for order command execution.
 * Each failure exposes the i18n message key used by the interface layer to build a localized response.
 *
 */
public sealed interface OrderCommandFailure
        permits OrderCommandFailure.OrderNotFound, OrderCommandFailure.InvalidOrderState, OrderCommandFailure.EmptyOrderItems {
    /**
     * @return message key associated with the failure
     */
    String messageKey();

    /**
     * Failure returned when the order of the command does not exist.
     */
    record OrderNotFound() implements OrderCommandFailure {
        @Override
        public String messageKey() {
            return "order.error.notFound";
        }
    }

    /**
     * Failure returned when the current status of the order does not allow the operation
     * (for example, adding an item to a confirmed order or cancelling an order in process).
     *
     * @param messageKey message key describing which status rule was violated
     */
    record InvalidOrderState(String messageKey) implements OrderCommandFailure {
    }

    /**
     * Failure returned when an order without items is confirmed.
     */
    record EmptyOrderItems() implements OrderCommandFailure {
        @Override
        public String messageKey() {
            return "order.error.items.empty";
        }
    }
}
