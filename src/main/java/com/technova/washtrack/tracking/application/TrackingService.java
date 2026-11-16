package com.technova.washtrack.tracking.application;

import java.time.Clock;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.technova.washtrack.shared.exception.BusinessRuleException;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;
import com.technova.washtrack.tracking.domain.event.OrderStatusUpdated;
import com.technova.washtrack.tracking.domain.model.OrderStage;
import com.technova.washtrack.tracking.domain.model.OrderTracking;
import com.technova.washtrack.tracking.domain.model.StageChange;
import com.technova.washtrack.tracking.domain.repository.OrderTrackingRepository;
import com.technova.washtrack.tracking.domain.repository.StageChangeRepository;

@Service
public class TrackingService {

    private final OrderTrackingRepository trackings;
    private final StageChangeRepository changes;
    private final Clock clock;
    private final ApplicationEventPublisher events;

    TrackingService(OrderTrackingRepository trackings, StageChangeRepository changes, Clock clock,
            ApplicationEventPublisher events) {
        this.trackings = trackings;
        this.changes = changes;
        this.clock = clock;
        this.events = events;
    }

    /** Starts following an order; it begins in the RECEIVED stage. */
    public OrderTracking start(Long orderId, String orderNumber, Long customerId) {
        if (trackings.findByOrderId(orderId).isPresent()) {
            throw new BusinessRuleException("Order " + orderId + " is already being tracked");
        }
        OrderTracking tracking = OrderTracking.start(trackings.nextId(), orderId, orderNumber, customerId,
                clock.instant());
        return recorded(tracking);
    }

    public OrderTracking advance(Long id) {
        OrderTracking tracking = getById(id);
        tracking.advance(clock.instant());
        return recorded(tracking);
    }

    /** Same as {@link #advance(Long)} but addressed by the order id. */
    public OrderTracking advanceOrder(Long orderId) {
        OrderTracking tracking = trackings.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking of order", orderId));
        tracking.advance(clock.instant());
        return recorded(tracking);
    }

    /** Marks an order as delivered, but only if it is being tracked and is ready for delivery; otherwise does nothing. */
    public void markDelivered(Long orderId) {
        trackings.findByOrderId(orderId).filter(t -> t.stage() == OrderStage.READY_FOR_DELIVERY).ifPresent(t -> {
            t.advance(clock.instant());
            recorded(t);
        });
    }

    public OrderTracking getById(Long id) {
        return trackings.findById(id).orElseThrow(() -> new ResourceNotFoundException("OrderTracking", id));
    }

    public List<OrderTracking> search(Long customerId) {
        return trackings.search(customerId);
    }

    /** Stage history of an order, oldest first. */
    public List<StageChange> history(Long orderId) {
        return changes.findByOrderId(orderId);
    }

    private OrderTracking recorded(OrderTracking tracking) {
        trackings.save(tracking);
        changes.save(new StageChange(changes.nextId(), tracking.orderId(), tracking.stage(), tracking.updatedAt()));
        events.publishEvent(new OrderStatusUpdated(tracking.orderId(), tracking.orderNumber(), tracking.customerId(),
                tracking.stage()));
        return tracking;
    }
}
