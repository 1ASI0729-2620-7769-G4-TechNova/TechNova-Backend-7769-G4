package com.technova.washtrack.billing.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import com.technova.washtrack.billing.domain.event.SubscriptionCancelled;
import com.technova.washtrack.billing.domain.event.SubscriptionRenewed;
import com.technova.washtrack.billing.domain.model.Subscription;
import com.technova.washtrack.billing.domain.model.SubscriptionStatus;
import com.technova.washtrack.shared.exception.BusinessRuleException;
import com.technova.washtrack.support.MutableClock;

/** Time-dependent behaviour (expiry, renewal) driven by a clock the test controls. */
@SpringBootTest
@RecordApplicationEvents
@Import(SubscriptionLifecycleTests.FrozenTime.class)
class SubscriptionLifecycleTests {

    private static final Instant T0 = Instant.parse("2026-09-14T00:00:00Z");
    private static final MutableClock CLOCK = new MutableClock(T0);

    @TestConfiguration
    static class FrozenTime {

        @Bean
        @Primary
        Clock testClock() {
            return CLOCK;
        }
    }

    @Autowired
    private SubscriptionService subscriptions;

    @Autowired
    private ApplicationEvents events;

    @BeforeEach
    void resetClock() {
        CLOCK.set(T0);
    }

    @Test
    void overdueSubscriptionIsReportedExpiredAndRenewedFromNow() {
        Subscription subscription = subscriptions.subscribe(701L, 2L);
        Instant later = Instant.parse("2026-10-20T00:00:00Z");
        CLOCK.set(later);

        assertEquals(SubscriptionStatus.EXPIRED, subscriptions.getById(subscription.id()).status());
        assertEquals(SubscriptionStatus.EXPIRED, subscriptions.findByLaundry(701L).get(0).status());

        Subscription renewed = subscriptions.renew(subscription.id());

        assertEquals(SubscriptionStatus.ACTIVE, renewed.status());
        assertEquals(later, renewed.startDate());
        assertEquals(Instant.parse("2026-11-20T00:00:00Z"), renewed.endDate());
        assertEquals(1, events.stream(SubscriptionRenewed.class).count());
    }

    @Test
    void featuresFollowTheActivePlanAndDisappearWhenItLapses() {
        assertEquals(java.util.List.of(), subscriptions.enabledFeatures(704L));

        subscriptions.subscribe(704L, 2L);
        assertEquals(java.util.List.of("orders", "reports", "digital-payments"), subscriptions.enabledFeatures(704L));

        CLOCK.set(Instant.parse("2026-12-01T00:00:00Z"));
        assertEquals(java.util.List.of(), subscriptions.enabledFeatures(704L));
    }

    @Test
    void renewingAnActiveSubscriptionExtendsItsEndDate() {
        Subscription subscription = subscriptions.subscribe(702L, 2L);

        Subscription renewed = subscriptions.renew(subscription.id());

        assertEquals(Instant.parse("2026-11-14T00:00:00Z"), renewed.endDate());
        assertEquals(T0, renewed.startDate());
    }

    @Test
    void cancelledSubscriptionCannotBeRenewedButLaundryCanSubscribeAgain() {
        Subscription subscription = subscriptions.subscribe(703L, 2L);

        assertEquals(SubscriptionStatus.CANCELLED, subscriptions.cancel(subscription.id()).status());
        assertEquals(1, events.stream(SubscriptionCancelled.class).count());
        assertThrows(BusinessRuleException.class, () -> subscriptions.renew(subscription.id()));
        assertThrows(BusinessRuleException.class, () -> subscriptions.cancel(subscription.id()));

        assertEquals(SubscriptionStatus.ACTIVE, subscriptions.subscribe(703L, 3L).status());
    }
}
