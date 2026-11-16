package com.technova.washtrack.orders.infrastructure.persistence.jpa;

import com.technova.washtrack.orders.domain.model.valueobjects.CustomerId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for persisting {@link CustomerId} value objects as numbers.
 *
 */
@Converter
public class CustomerIdAttributeConverter implements AttributeConverter<CustomerId, Long> {
    /**
     * Converts the customer identifier to its database column value.
     *
     */
    @Override
    public Long convertToDatabaseColumn(CustomerId attribute) {
        return attribute == null ? null : attribute.value();
    }

    /**
     * Converts the database column value to a customer identifier.
     *
     */
    @Override
    public CustomerId convertToEntityAttribute(Long databaseData) {
        return databaseData == null ? null : new CustomerId(databaseData);
    }
}
