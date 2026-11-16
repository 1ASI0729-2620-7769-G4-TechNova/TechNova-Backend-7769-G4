package com.technova.washtrack.orders.application.internal.queryservices;

import com.technova.washtrack.orders.application.queryservices.OrderQueryService;
import com.technova.washtrack.orders.domain.model.aggregates.Order;
import com.technova.washtrack.orders.domain.model.queries.GetOrderByIdQuery;
import com.technova.washtrack.orders.domain.model.queries.GetOrdersByCustomerIdQuery;
import com.technova.washtrack.orders.domain.model.queries.GetOrdersByLaundryIdQuery;
import com.technova.washtrack.orders.infrastructure.persistence.jpa.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service for order query operations.
 * Contains query handler methods that translate domain query specifications
 * into persistence operations, returning domain models from storage.
 *
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class OrderQueryServiceImpl implements OrderQueryService {
    private final OrderRepository orderRepository;

    public OrderQueryServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    @Override
    public Optional<Order> handle(GetOrderByIdQuery query) {
        log.debug("Querying order by id={}", query.orderId());
        var result = orderRepository.findById(query.orderId());
        if (result.isEmpty()) log.debug("No order found for id={}", query.orderId());
        return result;
    }


    @Override
    public List<Order> handle(GetOrdersByCustomerIdQuery query) {
        log.debug("Querying all orders for customerId={}", query.customerId().value());
        var results = orderRepository.findAllByCustomerId(query.customerId());
        log.debug("Found {} order(s) for customerId={}", results.size(), query.customerId().value());
        return results;
    }


    @Override
    public List<Order> handle(GetOrdersByLaundryIdQuery query) {
        log.debug("Querying all orders for laundryId={}", query.laundryId().value());
        var results = orderRepository.findAllByLaundryId(query.laundryId());
        log.debug("Found {} order(s) for laundryId={}", results.size(), query.laundryId().value());
        return results;
    }
}
