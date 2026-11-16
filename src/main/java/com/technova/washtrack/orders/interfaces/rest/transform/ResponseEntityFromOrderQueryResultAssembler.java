package com.technova.washtrack.orders.interfaces.rest.transform;

import com.technova.washtrack.orders.domain.model.aggregates.Order;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Interface layer translator converting order query results into HTTP ResponseEntity values.
 *
 */
public class ResponseEntityFromOrderQueryResultAssembler {
    /**
     * Converts a single order into a 200 response.
     *
     */
    public static ResponseEntity<?> toResponseEntityFromOrder(Order order) {
        return ResponseEntity.ok(OrderResourceFromEntityAssembler.toResourceFromEntity(order));
    }

    /**
     * Converts a list query result into a 200 response.
     *
     */
    public static ResponseEntity<?> toResponseEntityFromList(List<Order> orders) {
        var orderResources = orders.stream()
                .map(OrderResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(orderResources);
    }

    /**
     * Builds a localized 400-response body.
     *
     */
    public static ResponseEntity<ProblemDetail> badRequest(MessageSource messageSource, String messageKey, Object... args) {
        return problemDetail(HttpStatus.BAD_REQUEST, messageSource, messageKey, args);
    }

    /**
     * Builds a localized 404-response body.
     *
     */
    public static ResponseEntity<ProblemDetail> notFound(MessageSource messageSource, String messageKey, Object... args) {
        return problemDetail(HttpStatus.NOT_FOUND, messageSource, messageKey, args);
    }

    private static ResponseEntity<ProblemDetail> problemDetail(HttpStatus status, MessageSource messageSource, String messageKey, Object... args) {
        var detail = messageSource.getMessage(messageKey, args, messageKey, LocaleContextHolder.getLocale());
        return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, detail));
    }
}
