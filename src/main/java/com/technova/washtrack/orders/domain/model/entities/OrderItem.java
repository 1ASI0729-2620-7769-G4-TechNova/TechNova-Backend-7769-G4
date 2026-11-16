package com.technova.washtrack.orders.domain.model.entities;

import com.technova.washtrack.orders.domain.model.valueobjects.GarmentType;
import com.technova.washtrack.orders.domain.model.valueobjects.Money;
import com.technova.washtrack.orders.domain.model.valueobjects.ServiceId;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Entity representing a group of garments of the same type registered in an order.
 * It is created and managed only through the {@code Order} aggregate root.
 * <p>
 * Invariants:
 * - Service, garment type and unit price are required
 * - The quantity must be greater than 0
 * - The unit price must be greater than 0
 * - The weight is optional and informative; when it is registered, it must be greater than 0
 * - The subtotal is always calculated as {@code unitPrice x quantity} and cannot be set from outside
 * <p>
 * The entity has no setters: once registered, its data only changes through the aggregate root.
 *
 */
@Getter
@Entity
public class OrderItem {
    private static final int NOTES_MAX_LENGTH = 255;
    private static final String SERVICE_ID_NOT_NULL_MESSAGE_KEY = "order.error.item.serviceId.notNull";
    private static final String GARMENT_TYPE_NOT_NULL_MESSAGE_KEY = "order.error.item.garmentType.notNull";
    private static final String QUANTITY_INVALID_MESSAGE_KEY = "order.error.item.quantity.invalid";
    private static final String WEIGHT_INVALID_MESSAGE_KEY = "order.error.item.weightKg.invalid";
    private static final String UNIT_PRICE_NOT_NULL_MESSAGE_KEY = "order.error.item.unitPrice.notNull";
    private static final String UNIT_PRICE_INVALID_MESSAGE_KEY = "order.error.item.unitPrice.invalid";
    private static final String NOTES_SIZE_MESSAGE_KEY = "order.error.item.notes.size";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "service_id", nullable = false))
    private ServiceId serviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GarmentType garmentType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 8, scale = 2)
    private BigDecimal weightKg;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount", nullable = false, precision = 10, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency", nullable = false, length = 3))
    })
    private Money unitPrice;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "subtotal_currency", nullable = false, length = 3))
    })
    private Money subtotal;

    @Column(length = NOTES_MAX_LENGTH)
    private String notes;

    protected OrderItem() {}

    /**
     * Creates an order item and calculates its subtotal.
     *
     * @param serviceId   identifier of the requested laundry service
     * @param garmentType type of garment
     * @param quantity    number of garments; it must be greater than 0
     * @param weightKg    optional weight in kilograms; when present, it must be greater than 0
     * @param unitPrice   price of one garment; it must be greater than 0
     * @param notes       optional notes about the garments
     * @throws IllegalArgumentException if any invariant of the item is violated
     */
    public OrderItem(ServiceId serviceId, GarmentType garmentType, Integer quantity, BigDecimal weightKg, Money unitPrice, String notes) {
        if (serviceId == null) {
            throw new IllegalArgumentException(SERVICE_ID_NOT_NULL_MESSAGE_KEY);
        }
        if (garmentType == null) {
            throw new IllegalArgumentException(GARMENT_TYPE_NOT_NULL_MESSAGE_KEY);
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(QUANTITY_INVALID_MESSAGE_KEY);
        }
        if (weightKg != null && weightKg.signum() <= 0) {
            throw new IllegalArgumentException(WEIGHT_INVALID_MESSAGE_KEY);
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException(UNIT_PRICE_NOT_NULL_MESSAGE_KEY);
        }
        if (unitPrice.amount().signum() <= 0) {
            throw new IllegalArgumentException(UNIT_PRICE_INVALID_MESSAGE_KEY);
        }
        if (notes != null && notes.length() > NOTES_MAX_LENGTH) {
            throw new IllegalArgumentException(NOTES_SIZE_MESSAGE_KEY);
        }
        this.serviceId = serviceId;
        this.garmentType = garmentType;
        this.quantity = quantity;
        this.weightKg = weightKg;
        this.unitPrice = unitPrice;
        this.notes = notes;
        this.subtotal = calculateSubtotal();
    }

    /**
     * Calculates the subtotal of the item as {@code unitPrice x quantity}.
     * The weight is informative and is not part of the calculation.
     *
     * @return subtotal of the item, in the currency of the unit price, rounded to two decimals
     */
    public Money calculateSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}