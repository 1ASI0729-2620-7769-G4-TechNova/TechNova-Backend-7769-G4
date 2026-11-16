package com.technova.washtrack.orders.infrastructure.persistence.jpa;

import com.technova.washtrack.orders.domain.model.valueobjects.LaundryId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for persisting {@link LaundryId} value objects as numbers.
 *
 */
@Converter
public class LaundryIdAttributeConverter implements AttributeConverter<LaundryId, Long> {
    /**
     * Converts the laundry identifier to its database column value.
     *
     */
    @Override
    public Long convertToDatabaseColumn(LaundryId attribute) {
        return attribute == null ? null : attribute.value();
    }

    /**
     * Converts the database column value to a laundry identifier.
     *
     */
    @Override
    public LaundryId convertToEntityAttribute(Long databaseData) {
        return databaseData == null ? null : new LaundryId(databaseData);
    }
}
