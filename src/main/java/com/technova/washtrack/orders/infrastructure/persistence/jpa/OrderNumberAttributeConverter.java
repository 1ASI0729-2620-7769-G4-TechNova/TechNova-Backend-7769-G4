package com.technova.washtrack.orders.infrastructure.persistence.jpa;

import com.technova.washtrack.orders.domain.model.valueobjects.OrderNumber;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for persisting {@link OrderNumber} value objects as strings.
 *
 */
@Converter
public class OrderNumberAttributeConverter implements AttributeConverter<OrderNumber, String> {
    /**
     * Converts the order number to its database column value.
     *
     * @param attribute order number value object
     */
    @Override
    public String convertToDatabaseColumn(OrderNumber attribute) {
        return attribute == null ? null : attribute.value();
    }

    /**
     * Converts the database column value to an order number.
     *
     * @param databaseData raw order number stored in the database
     */
    @Override
    public OrderNumber convertToEntityAttribute(String databaseData) {
        return databaseData == null ? null : new OrderNumber(databaseData);
    }
}
