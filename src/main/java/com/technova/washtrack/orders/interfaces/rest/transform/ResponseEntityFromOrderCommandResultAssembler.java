package com.technova.washtrack.orders.interfaces.rest.transform;

import com.technova.washtrack.orders.application.commandservices.OrderCommandFailure;
import com.technova.washtrack.orders.domain.model.aggregates.Order;
import com.technova.washtrack.shared.application.result.Result;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Interface layer translator converting an order command result into an HTTP ResponseEntity.
 *
 */
public class ResponseEntityFromOrderCommandResultAssembler {
    /**
     * Converts an order command result to a ResponseEntity.
     *
     */
    public static ResponseEntity<?> toResponseEntityFromResult(
            Result<Order, OrderCommandFailure> result,
            MessageSource messageSource,
            HttpStatus successStatus) {
        return result.fold(
                order -> new ResponseEntity<>(OrderResourceFromEntityAssembler.toResourceFromEntity(order), successStatus),
                failure -> {
                    var status = statusFromFailure(failure);
                    return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(
                            status,
                            localizedMessage(messageSource, failure.messageKey())));
                });
    }

    private static HttpStatus statusFromFailure(OrderCommandFailure failure) {
        if (failure instanceof OrderCommandFailure.OrderNotFound) {
            return HttpStatus.NOT_FOUND;
        }
        if (failure instanceof OrderCommandFailure.InvalidOrderState) {
            return HttpStatus.CONFLICT;
        }
        if (failure instanceof OrderCommandFailure.EmptyOrderItems) {
            return HttpStatus.UNPROCESSABLE_CONTENT;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private static String localizedMessage(MessageSource messageSource, String messageKey) {
        return messageSource.getMessage(messageKey, null, messageKey, LocaleContextHolder.getLocale());
    }
}
