package com.technova.washtrack.orders.interfaces.rest.transform;

import com.technova.washtrack.orders.domain.model.valueobjects.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Interface layer translator converting primitive values from HTTP inputs into domain value objects.
 *
 */
public class OrderValueObjectFromPrimitiveAssembler {
    private static final String GARMENT_TYPE_NOT_NULL_MESSAGE_KEY = "order.error.item.garmentType.notNull";
    private static final String GARMENT_TYPE_INVALID_MESSAGE_KEY = "order.error.item.garmentType.invalid";

    /**
     * Converts a raw number into a {@link CustomerId} value object.
     *
     */
    public static CustomerId toCustomerId(Long value) {
        return new CustomerId(value);
    }

    /**
     * Converts a raw number into a {@link LaundryId} value object.
     *
     */
    public static LaundryId toLaundryId(Long value) {
        return new LaundryId(value);
    }

    /**
     * Converts a raw number into a {@link ServiceId} value object.
     *
     */
    public static ServiceId toServiceId(Long value) {
        return new ServiceId(value);
    }

    /**
     * Converts a raw amount and currency into a {@link Money} value object.
     *
     */
    public static Money toMoney(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    /**
     * Converts a raw date and time into a {@link PromisedDate} value object.
     *
     */
    public static PromisedDate toPromisedDate(LocalDateTime value) {
        return new PromisedDate(value);
    }

    /**
     * Converts a raw text into a {@link GarmentType}, ignoring upper and lower case.
     *
     */
    public static GarmentType toGarmentType(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(GARMENT_TYPE_NOT_NULL_MESSAGE_KEY);
        }
        for (GarmentType garmentType : GarmentType.values()) {
            if (garmentType.name().equalsIgnoreCase(value.trim())) {
                return garmentType;
            }
        }
        throw new IllegalArgumentException(GARMENT_TYPE_INVALID_MESSAGE_KEY);
    }
}
