package com.technova.washtrack.laundryoperations.application;

import java.time.Clock;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.technova.washtrack.laundryoperations.domain.event.LaundryOrderReadyForDelivery;
import com.technova.washtrack.laundryoperations.domain.event.LaundryOrderReceived;
import com.technova.washtrack.laundryoperations.domain.event.LaundryOrderStageAdvanced;
import com.technova.washtrack.laundryoperations.domain.model.LaundryOrder;
import com.technova.washtrack.laundryoperations.domain.model.LaundryStatus;
import com.technova.washtrack.laundryoperations.domain.repository.LaundryOrderRepository;
import com.technova.washtrack.shared.exception.BusinessRuleException;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;

@Service
public class LaundryOrderService {

    private final LaundryOrderRepository laundryOrders;
    private final Clock clock;
    private final ApplicationEventPublisher events;

    LaundryOrderService(LaundryOrderRepository laundryOrders, Clock clock, ApplicationEventPublisher events) {
        this.laundryOrders = laundryOrders;
        this.clock = clock;
        this.events = events;
    }

    /** Registers the reception of an order; each original order can only be received once. */
    public LaundryOrder receive(Long orderId, String orderNumber, Long customerId, int garmentCount) {
        if (laundryOrders.findByOrderId(orderId).isPresent()) {
            throw new BusinessRuleException("Order " + orderId + " has already been received by the laundry");
        }
        LaundryOrder created = laundryOrders.save(LaundryOrder.receive(laundryOrders.nextId(), orderId,
                orderNumber, customerId, garmentCount, clock.instant()));
        events.publishEvent(new LaundryOrderReceived(created.id(), created.orderId(), created.orderNumber(),
                created.customerId(), created.garmentCount()));
        return created;
    }

    /** Advances a laundry order to its next processing stage and publishes domain events. */
    public LaundryOrder advance(Long id) {
        LaundryOrder order = getById(id);
        LaundryStatus newStatus = order.advance(clock.instant());
        laundryOrders.save(order);
        events.publishEvent(new LaundryOrderStageAdvanced(order.id(), order.orderId(), order.orderNumber(),
                order.customerId(), newStatus));
        if (order.isReadyForDelivery()) {
            events.publishEvent(new LaundryOrderReadyForDelivery(order.id(), order.orderId(),
                    order.orderNumber(), order.customerId()));
        }
        return order;
    }

    public LaundryOrder getById(Long id) {
        return laundryOrders.findById(id).orElseThrow(() -> new ResourceNotFoundException("LaundryOrder", id));
    }

    public List<LaundryOrder> search(LaundryStatus status, Long customerId) {
        return laundryOrders.search(status, customerId);
    }
}
