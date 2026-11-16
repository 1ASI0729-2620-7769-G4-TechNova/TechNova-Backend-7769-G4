package com.technova.washtrack.orders.interfaces.rest;

import com.technova.washtrack.orders.application.commandservices.OrderCommandService;
import com.technova.washtrack.orders.application.queryservices.OrderQueryService;
import com.technova.washtrack.orders.domain.model.aggregates.Order;
import com.technova.washtrack.orders.domain.model.commands.CancelOrderCommand;
import com.technova.washtrack.orders.domain.model.commands.ConfirmOrderCommand;
import com.technova.washtrack.orders.domain.model.queries.GetOrderByIdQuery;
import com.technova.washtrack.orders.domain.model.queries.GetOrdersByCustomerIdQuery;
import com.technova.washtrack.orders.domain.model.queries.GetOrdersByLaundryIdQuery;
import com.technova.washtrack.orders.interfaces.rest.resources.AddOrderItemResource;
import com.technova.washtrack.orders.interfaces.rest.resources.CancelOrderResource;
import com.technova.washtrack.orders.interfaces.rest.resources.CreateOrderResource;
import com.technova.washtrack.orders.interfaces.rest.resources.OrderResource;
import com.technova.washtrack.orders.interfaces.rest.transform.AddOrderItemCommandFromResourceAssembler;
import com.technova.washtrack.orders.interfaces.rest.transform.CreateOrderCommandFromResourceAssembler;
import com.technova.washtrack.orders.interfaces.rest.transform.OrderValueObjectFromPrimitiveAssembler;
import com.technova.washtrack.orders.interfaces.rest.transform.ResponseEntityFromOrderCommandResultAssembler;
import com.technova.washtrack.orders.interfaces.rest.transform.ResponseEntityFromOrderQueryResultAssembler;
import com.technova.washtrack.shared.application.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Inbound service in the interface layer for the Order Management bounded context.
 *
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/orders", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Orders", description = "Endpoints for orders (Order Management)")
public class OrdersController {
    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;
    private final MessageSource messageSource;

    /**
     * Constructor for OrdersController.
     *
     */
    public OrdersController(OrderCommandService orderCommandService, OrderQueryService orderQueryService, MessageSource messageSource) {
        this.orderCommandService = orderCommandService;
        this.orderQueryService = orderQueryService;
        this.messageSource = messageSource;
    }

    /**
     * Creates a new order in DRAFT status.
     *
     */
    @Operation(
            summary = "Create an order",
            description = "Creates an order in DRAFT status with a unique order number and calculates its total amount",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Order creation request",
                    content = @Content(schema = @Schema(implementation = CreateOrderResource.class))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created",
                    content = @Content(schema = @Schema(implementation = OrderResource.class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderResource resource) {
        log.debug("POST /api/v1/orders – customerId={}, laundryId={}", resource.customerId(), resource.laundryId());
        var result = orderCommandService.handle(CreateOrderCommandFromResourceAssembler.toCommandFromResource(resource));
        var response = ResponseEntityFromOrderCommandResultAssembler.toResponseEntityFromResult(result, messageSource, HttpStatus.CREATED);
        log.debug("POST /api/v1/orders – response status={}", response.getStatusCode());
        return response;
    }

    /**
     * Adds an item to an order in DRAFT status and recalculates its total amount.
     *
     */
    @Operation(
            summary = "Add an item to an order",
            description = "Adds an item to an order in DRAFT status and recalculates the total amount",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Order item request",
                    content = @Content(schema = @Schema(implementation = AddOrderItemResource.class))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item added",
                    content = @Content(schema = @Schema(implementation = OrderResource.class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - the order is not in DRAFT status",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("{orderId}/items")
    public ResponseEntity<?> addOrderItem(@PathVariable Long orderId, @Valid @RequestBody AddOrderItemResource resource) {
        log.debug("POST /api/v1/orders/{}/items", orderId);
        var result = orderCommandService.handle(AddOrderItemCommandFromResourceAssembler.toCommandFromResource(orderId, resource));
        return ResponseEntityFromOrderCommandResultAssembler.toResponseEntityFromResult(result, messageSource, HttpStatus.CREATED);
    }

    /**
     * Confirms an order in DRAFT status that has at least one item.
     *
     */
    @Operation(
            summary = "Confirm an order",
            description = "Confirms an order in DRAFT status that has at least one item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order confirmed",
                    content = @Content(schema = @Schema(implementation = OrderResource.class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - the order is not in DRAFT status",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "The order has no items",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("{orderId}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Long orderId) {
        log.debug("POST /api/v1/orders/{}/confirm", orderId);
        var result = orderCommandService.handle(new ConfirmOrderCommand(orderId));
        return ResponseEntityFromOrderCommandResultAssembler.toResponseEntityFromResult(result, messageSource, HttpStatus.OK);
    }

    /**
     * Cancels an order in DRAFT or CONFIRMED status.
     *
     */
    @Operation(
            summary = "Cancel an order",
            description = "Cancels an order that has not entered processing yet (DRAFT or CONFIRMED status)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Order cancellation request",
                    content = @Content(schema = @Schema(implementation = CancelOrderResource.class))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled",
                    content = @Content(schema = @Schema(implementation = OrderResource.class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - the order already entered processing",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId, @Valid @RequestBody CancelOrderResource resource) {
        log.debug("POST /api/v1/orders/{}/cancel", orderId);
        var result = orderCommandService.handle(new CancelOrderCommand(orderId, resource.reason()));
        return ResponseEntityFromOrderCommandResultAssembler.toResponseEntityFromResult(result, messageSource, HttpStatus.OK);
    }

    /**
     * Gets an order by ID.
     *
     */
    @Operation(
            summary = "Get an order by ID",
            description = "Gets an order with its items, total amount, status and promised date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(schema = @Schema(implementation = OrderResource.class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        log.debug("GET /api/v1/orders/{}", orderId);
        Optional<Order> order = orderQueryService.handle(new GetOrderByIdQuery(orderId));
        if (order.isEmpty()) {
            log.debug("Order not found for id={}", orderId);
            return ResponseEntityFromOrderQueryResultAssembler.notFound(
                    messageSource,
                    "order.error.notFoundById",
                    orderId);
        }
        return ResponseEntityFromOrderQueryResultAssembler.toResponseEntityFromOrder(order.orElseThrow());
    }

    /**
     * Retrieves orders using query parameters.
     *
     */
    @Operation(
            summary = "Get orders by customer or laundry",
            description = "Gets the orders of a customer (customerId) or the orders received by a laundry (laundryId)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders found (possibly empty)",
                    content = @Content(schema = @Schema(implementation = OrderResource[].class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<?> getOrdersByCustomerOrLaundry(
            @Parameter(name = "customerId", description = "Customer ID")
            @RequestParam(name = "customerId", required = false) Long customerId,
            @Parameter(name = "laundryId", description = "Laundry ID")
            @RequestParam(name = "laundryId", required = false) Long laundryId) {
        if (customerId != null && laundryId != null) {
            log.warn("GET /api/v1/orders – both customerId and laundryId were provided");
            return ResponseEntityFromOrderQueryResultAssembler.badRequest(messageSource, "order.error.query.filter.both");
        }
        if (customerId != null) {
            log.debug("GET /api/v1/orders?customerId={}", customerId);
            var orders = orderQueryService.handle(new GetOrdersByCustomerIdQuery(
                    OrderValueObjectFromPrimitiveAssembler.toCustomerId(customerId)));
            return ResponseEntityFromOrderQueryResultAssembler.toResponseEntityFromList(orders);
        }
        if (laundryId != null) {
            log.debug("GET /api/v1/orders?laundryId={}", laundryId);
            var orders = orderQueryService.handle(new GetOrdersByLaundryIdQuery(
                    OrderValueObjectFromPrimitiveAssembler.toLaundryId(laundryId)));
            return ResponseEntityFromOrderQueryResultAssembler.toResponseEntityFromList(orders);
        }
        log.warn("GET /api/v1/orders – missing required parameter: customerId or laundryId");
        return ResponseEntityFromOrderQueryResultAssembler.badRequest(messageSource, "order.error.query.filter.missing");
    }
}
