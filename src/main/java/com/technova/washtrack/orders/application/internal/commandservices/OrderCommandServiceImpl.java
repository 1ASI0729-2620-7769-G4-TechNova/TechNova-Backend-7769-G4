package com.technova.washtrack.orders.application.internal.commandservices;

import com.technova.washtrack.orders.application.commandservices.OrderCommandFailure;
import com.technova.washtrack.orders.application.commandservices.OrderCommandService;
import com.technova.washtrack.orders.domain.model.aggregates.Order;
import com.technova.washtrack.orders.domain.model.commands.AddOrderItemCommand;
import com.technova.washtrack.orders.domain.model.commands.CancelOrderCommand;
import com.technova.washtrack.orders.domain.model.commands.ConfirmOrderCommand;
import com.technova.washtrack.orders.domain.model.commands.CreateOrderCommand;
import com.technova.washtrack.orders.domain.model.valueobjects.OrderNumber;
import com.technova.washtrack.orders.domain.model.valueobjects.OrderStatus;
import com.technova.washtrack.orders.infrastructure.persistence.jpa.OrderRepository;
import com.technova.washtrack.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for order command operations.
 * Contains the command handler methods that load the Order aggregate, check the
 * business rules that depend on its current status and delegate the change to the aggregate.
 * Status rule violations are returned as application-layer failures instead of raising exceptions.
 *
 */
@Slf4j
@Service
public class OrderCommandServiceImpl implements OrderCommandService {
    private static final String ORDER_NUMBER_PREFIX = "ORD-";
    private static final String ORDER_NUMBER_SEQUENCE_FORMAT = "%04d";
    private static final String NOT_DRAFT_MESSAGE_KEY = "order.error.status.notDraft";
    private static final String NOT_CANCELLABLE_MESSAGE_KEY = "order.error.status.notCancellable";

    private final OrderRepository orderRepository;

    public OrderCommandServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    @Override
    @Transactional
    public Result<Order, OrderCommandFailure> handle(CreateOrderCommand command) {
        var orderNumber = generateUniqueOrderNumber();
        var order = new Order(command, orderNumber);
        var createdOrder = orderRepository.save(order);
        log.info("Order created: id={}, orderNumber={}, customerId={}, laundryId={}, totalAmount={} {}",
                createdOrder.getId(),
                createdOrder.getOrderNumber().value(),
                createdOrder.getCustomerId().value(),
                createdOrder.getLaundryId().value(),
                createdOrder.getTotalAmount().amount(),
                createdOrder.getTotalAmount().currency());
        return Result.success(createdOrder);
    }


    @Override
    @Transactional
    public Result<Order, OrderCommandFailure> handle(AddOrderItemCommand command) {
        var order = orderRepository.findById(command.orderId());
        if (order.isEmpty()) {
            log.warn("Order not found when adding an item: id={}", command.orderId());
            return Result.failure(new OrderCommandFailure.OrderNotFound());
        }
        var existingOrder = order.get();
        if (existingOrder.getStatus() != OrderStatus.DRAFT) {
            log.warn("Item rejected because the order is not in DRAFT status: id={}, status={}",
                    existingOrder.getId(), existingOrder.getStatus());
            return Result.failure(new OrderCommandFailure.InvalidOrderState(NOT_DRAFT_MESSAGE_KEY));
        }
        existingOrder.addItem(
                command.serviceId(),
                command.garmentType(),
                command.quantity(),
                command.weightKg(),
                command.unitPrice(),
                command.notes());
        log.info("Item added to order: id={}, items={}, totalAmount={}",
                existingOrder.getId(), existingOrder.getItems().size(), existingOrder.getTotalAmount().amount());
        return Result.success(existingOrder);
    }


    @Override
    @Transactional
    public Result<Order, OrderCommandFailure> handle(ConfirmOrderCommand command) {
        var order = orderRepository.findById(command.orderId());
        if (order.isEmpty()) {
            log.warn("Order not found when confirming: id={}", command.orderId());
            return Result.failure(new OrderCommandFailure.OrderNotFound());
        }
        var existingOrder = order.get();
        if (existingOrder.getStatus() != OrderStatus.DRAFT) {
            log.warn("Confirmation rejected because the order is not in DRAFT status: id={}, status={}",
                    existingOrder.getId(), existingOrder.getStatus());
            return Result.failure(new OrderCommandFailure.InvalidOrderState(NOT_DRAFT_MESSAGE_KEY));
        }
        if (existingOrder.getItems().isEmpty()) {
            log.warn("Confirmation rejected because the order has no items: id={}", existingOrder.getId());
            return Result.failure(new OrderCommandFailure.EmptyOrderItems());
        }
        existingOrder.confirm();
        log.info("Order confirmed: id={}, orderNumber={}", existingOrder.getId(), existingOrder.getOrderNumber().value());
        return Result.success(existingOrder);
    }


    @Override
    @Transactional
    public Result<Order, OrderCommandFailure> handle(CancelOrderCommand command) {
        var order = orderRepository.findById(command.orderId());
        if (order.isEmpty()) {
            log.warn("Order not found when cancelling: id={}", command.orderId());
            return Result.failure(new OrderCommandFailure.OrderNotFound());
        }
        var existingOrder = order.get();
        if (existingOrder.getStatus() != OrderStatus.DRAFT && existingOrder.getStatus() != OrderStatus.CONFIRMED) {
            log.warn("Cancellation rejected because the order already entered processing: id={}, status={}",
                    existingOrder.getId(), existingOrder.getStatus());
            return Result.failure(new OrderCommandFailure.InvalidOrderState(NOT_CANCELLABLE_MESSAGE_KEY));
        }
        existingOrder.cancel(command.reason());
        log.info("Order cancelled: id={}, orderNumber={}", existingOrder.getId(), existingOrder.getOrderNumber().value());
        return Result.success(existingOrder);
    }

    /**
     * Generates the next available order number with the format {@code ORD-0001}.
     * It starts from the number of registered orders plus one and skips any number already in use.
     *
     * @return unique order number
     */
    private OrderNumber generateUniqueOrderNumber() {
        var sequence = orderRepository.count() + 1;
        var orderNumber = new OrderNumber(ORDER_NUMBER_PREFIX + String.format(ORDER_NUMBER_SEQUENCE_FORMAT, sequence));
        while (orderRepository.existsByOrderNumber(orderNumber)) {
            sequence++;
            orderNumber = new OrderNumber(ORDER_NUMBER_PREFIX + String.format(ORDER_NUMBER_SEQUENCE_FORMAT, sequence));
        }
        return orderNumber;
    }
}
