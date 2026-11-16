package com.technova.washtrack.orders.domain.model.aggregates;

import com.technova.washtrack.orders.domain.model.commands.CreateOrderCommand;
import com.technova.washtrack.orders.domain.model.commands.CreateOrderItemData;
import com.technova.washtrack.orders.domain.model.entities.OrderItem;
import com.technova.washtrack.orders.domain.model.valueobjects.*;
import com.technova.washtrack.orders.infrastructure.persistence.jpa.CustomerIdAttributeConverter;
import com.technova.washtrack.orders.infrastructure.persistence.jpa.LaundryIdAttributeConverter;
import com.technova.washtrack.orders.infrastructure.persistence.jpa.OrderNumberAttributeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate root of the Order Management bounded context.
 * <p>
 * An Order groups the garments and services requested by a customer to a laundry, the total amount
 * to be charged and the promised delivery date. The aggregate maintains these invariants:
 * - The order number is unique (enforced together with the persistence layer)
 * - The promised date must be in the future when the order is created
 * - Items can only be added while the order is in {@code DRAFT} status
 * - Every item must use the same currency as the order
 * - The total amount is always the sum of the item subtotals and is recalculated automatically
 * - An order can only be confirmed when it is in {@code DRAFT} status and has at least one item
 * - An order can only be cancelled, with a reason, while it is in {@code DRAFT} or {@code CONFIRMED} status
 * <p>
 * Customers, laundries and services belong to other bounded contexts and are referenced only by
 * their identifiers ({@link CustomerId}, {@link LaundryId}, {@link ServiceId}).
 * The aggregate has no setters: its state only changes through its domain methods.
 * <p>
 * Created by {@link CreateOrderCommand}.
 *
 */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"order_number"}, name = Order.ORDER_NUMBER_UNIQUE_CONSTRAINT)
})
public class Order extends AbstractAggregateRoot<Order> {

    /** Unique constraint name shared with the persistence layer. */
    public static final String ORDER_NUMBER_UNIQUE_CONSTRAINT = "uk_order_order_number";

    private static final int SPECIAL_INSTRUCTIONS_MAX_LENGTH = 500;
    private static final int CANCELLATION_REASON_MAX_LENGTH = 255;
    private static final String ORDER_NUMBER_NOT_NULL_MESSAGE_KEY = "order.error.orderNumber.notBlank";
    private static final String PROMISED_DATE_PAST_MESSAGE_KEY = "order.error.promisedDate.past";
    private static final String SPECIAL_INSTRUCTIONS_SIZE_MESSAGE_KEY = "order.error.specialInstructions.size";
    private static final String CURRENCY_MISMATCH_MESSAGE_KEY = "order.error.item.currency.mismatch";
    private static final String NOT_DRAFT_MESSAGE_KEY = "order.error.status.notDraft";
    private static final String EMPTY_ITEMS_MESSAGE_KEY = "order.error.items.empty";
    private static final String NOT_CANCELLABLE_MESSAGE_KEY = "order.error.status.notCancellable";
    private static final String CANCELLATION_REASON_NOT_BLANK_MESSAGE_KEY = "order.error.cancellationReason.notBlank";
    private static final String CANCELLATION_REASON_SIZE_MESSAGE_KEY = "order.error.cancellationReason.size";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 20)
    @Convert(converter = OrderNumberAttributeConverter.class)
    private OrderNumber orderNumber;

    @Column(nullable = false, updatable = false)
    @Convert(converter = CustomerIdAttributeConverter.class)
    private CustomerId customerId;

    @Column(nullable = false, updatable = false)
    @Convert(converter = LaundryIdAttributeConverter.class)
    private LaundryId laundryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "promised_date", nullable = false))
    private PromisedDate promisedDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", nullable = false, length = 3))
    })
    private Money totalAmount;

    @Column(length = SPECIAL_INSTRUCTIONS_MAX_LENGTH)
    private String specialInstructions;

    @Column(length = CANCELLATION_REASON_MAX_LENGTH)
    private String cancellationReason;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

    protected Order() {}


    public Order(CreateOrderCommand command, OrderNumber orderNumber) {
        if (orderNumber == null) {
            throw new IllegalArgumentException(ORDER_NUMBER_NOT_NULL_MESSAGE_KEY);
        }
        if (command.promisedDate().isOverdue()) {
            throw new IllegalArgumentException(PROMISED_DATE_PAST_MESSAGE_KEY);
        }
        if (command.specialInstructions() != null && command.specialInstructions().length() > SPECIAL_INSTRUCTIONS_MAX_LENGTH) {
            throw new IllegalArgumentException(SPECIAL_INSTRUCTIONS_SIZE_MESSAGE_KEY);
        }
        this.orderNumber = orderNumber;
        this.customerId = command.customerId();
        this.laundryId = command.laundryId();
        this.promisedDate = command.promisedDate();
        this.specialInstructions = command.specialInstructions();
        this.status = OrderStatus.DRAFT;
        this.totalAmount = new Money(BigDecimal.ZERO, command.currency());
        for (CreateOrderItemData item : command.items()) {
            addItem(item.serviceId(), item.garmentType(), item.quantity(), item.weightKg(), item.unitPrice(), item.notes());
        }
    }

    /**
     * Adds an item to the order and recalculates the total amount.
     */
    public void addItem(ServiceId serviceId, GarmentType garmentType, Integer quantity, BigDecimal weightKg, Money unitPrice, String notes) {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException(NOT_DRAFT_MESSAGE_KEY);
        }
        if (unitPrice != null && !unitPrice.currency().equals(totalAmount.currency())) {
            throw new IllegalArgumentException(CURRENCY_MISMATCH_MESSAGE_KEY);
        }
        items.add(new OrderItem(serviceId, garmentType, quantity, weightKg, unitPrice, notes));
        recalculateTotal();
    }

    /**
     * Confirms the order, so no more items can be added.
     *
     * @throws IllegalStateException if the order is not in {@code DRAFT} status or has no items
     */
    public void confirm() {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException(NOT_DRAFT_MESSAGE_KEY);
        }
        if (items.isEmpty()) {
            throw new IllegalStateException(EMPTY_ITEMS_MESSAGE_KEY);
        }
        this.status = OrderStatus.CONFIRMED;
    }

    /**
     * Cancels the order and keeps the reason of the cancellation.
     * An order can only be cancelled before it enters processing.
     *
     * @param reason reason for the cancellation
     * @throws IllegalStateException    if the order is not in {@code DRAFT} or {@code CONFIRMED} status
     * @throws IllegalArgumentException if the reason is blank or too long
     */
    public void cancel(String reason) {
        if (status != OrderStatus.DRAFT && status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(NOT_CANCELLABLE_MESSAGE_KEY);
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(CANCELLATION_REASON_NOT_BLANK_MESSAGE_KEY);
        }
        if (reason.trim().length() > CANCELLATION_REASON_MAX_LENGTH) {
            throw new IllegalArgumentException(CANCELLATION_REASON_SIZE_MESSAGE_KEY);
        }
        this.cancellationReason = reason.trim();
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * Recalculates the total amount of the order as the sum of the subtotals of all its items.
     * An order without items has a total amount of 0.00.
     */
    public void recalculateTotal() {
        var recalculatedTotal = new Money(BigDecimal.ZERO, totalAmount.currency());
        for (OrderItem item : items) {
            recalculatedTotal = recalculatedTotal.add(item.getSubtotal());
        }
        this.totalAmount = recalculatedTotal;
    }

    /**
     * Gets the items of the order.
     * The returned list is read-only, so items can only be added through {@link #addItem}.
     *
     * @return read-only list of the order items
     */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}