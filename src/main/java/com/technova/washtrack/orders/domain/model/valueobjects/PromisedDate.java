package com.technova.washtrack.orders.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

/**
 * Value object representing the date and time the laundry commits to deliver the order.
 *
 * @param value promised delivery date and time
 *
 */
@Embeddable
public record PromisedDate(LocalDateTime value) {
    private static final String NOT_NULL_MESSAGE_KEY = "order.error.promisedDate.notNull";

    /**
     * Validates the promised date.
     *
     * @throws IllegalArgumentException if the value is null
     */
    public PromisedDate {
        if (value == null) {
            throw new IllegalArgumentException(NOT_NULL_MESSAGE_KEY);
        }
    }

    /**
     * Checks whether the promised date has already passed.
     *
     * @return {@code true} when the promised date is before the current date and time; otherwise {@code false}
     */
    public boolean isOverdue() {
        return value.isBefore(LocalDateTime.now());
    }
}