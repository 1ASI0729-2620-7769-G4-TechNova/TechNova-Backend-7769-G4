package com.technova.washtrack.billing.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.technova.washtrack.billing.domain.model.Subscription;
import com.technova.washtrack.billing.domain.model.SubscriptionStatus;
import com.technova.washtrack.shared.exception.BusinessRuleException;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;

/** Each test uses its own laundry id because the in-memory store is shared across the test context. */
@SpringBootTest
class SubscriptionServiceTests {

    @Autowired
    private SubscriptionService subscriptions;

    @Test
    void subscribesALaundryToAPlan() {
        Subscription subscription = subscriptions.subscribe(501L, 2L);

        assertEquals(SubscriptionStatus.ACTIVE, subscription.status());
        assertEquals(2L, subscription.planId());
        assertEquals(subscription.id(), subscriptions.getById(subscription.id()).id());
    }

    @Test
    void aLaundryCannotHoldTwoActiveSubscriptions() {
        subscriptions.subscribe(502L, 1L);

        assertThrows(BusinessRuleException.class, () -> subscriptions.subscribe(502L, 3L));
    }

    @Test
    void rejectsUnknownPlanAndUnknownSubscription() {
        assertThrows(ResourceNotFoundException.class, () -> subscriptions.subscribe(503L, 99L));
        assertThrows(ResourceNotFoundException.class, () -> subscriptions.getById(999999L));
    }

    @Test
    void listsASubscriptionsOfALaundryNewestFirst() {
        assertEquals(0, subscriptions.findByLaundry(504L).size());

        Subscription created = subscriptions.subscribe(504L, 2L);

        assertEquals(created.id(), subscriptions.findByLaundry(504L).get(0).id());
    }
}
