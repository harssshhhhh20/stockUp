package com.stockup.backend.domain.bharosa;

import com.stockup.backend.domain.reservation.event.ReservationEventRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Regression: the per-customer cap once counted raw events instead of whole
 * interactions.
 *
 * Because events arrive in funnel order, a cap of three kept
 * BROADCAST / VIEWED / OFFER and discarded the HANDOVER that came after — so
 * every shop scored as though it had never completed anything. The live score
 * read 25 with "completes 0% of reservations" while the database plainly showed
 * completed handovers.
 */
class BharosaCapRegressionTest {

    private record Row(UUID customerId, UUID basketId, String eventType, Instant occurredAt)
            implements ReservationEventRepository.CustomerEventRow {
        public UUID getCustomerId() { return customerId; }
        public UUID getBasketId() { return basketId; }
        public String getEventType() { return eventType; }
        public Instant getOccurredAt() { return occurredAt; }
    }

    @Test
    void aCompletedHandoverIsNotTruncatedAwayByTheCap() {
        UUID merchant = UUID.randomUUID();
        UUID customer = UUID.randomUUID();
        UUID basket = UUID.randomUUID();
        Instant now = Instant.now();

        // A full, ordinary funnel — five events for one request, which is more
        // than the per-customer cap of three.
        List<ReservationEventRepository.CustomerEventRow> rows = List.of(
                new Row(customer, basket, "REQUEST_BROADCAST", now.minusSeconds(500)),
                new Row(customer, basket, "MERCHANT_VIEWED", now.minusSeconds(400)),
                new Row(customer, basket, "OFFER_SUBMITTED", now.minusSeconds(300)),
                new Row(customer, basket, "CUSTOMER_RESERVED", now.minusSeconds(200)),
                new Row(customer, basket, "HANDOVER_COMPLETED", now.minusSeconds(100))
        );

        var repo = mock(ReservationEventRepository.class);
        when(repo.findScoreableEvents(any(), any())).thenReturn(rows);
        when(repo.medianResponseSeconds(any(), any())).thenReturn(120.0);
        when(repo.distinctCompletedCustomers(any(), any())).thenReturn(1L);

        var engine = new BharosaEngine(repo, null, new BharosaWeights());
        var pillars = engine.computePillars(merchant);

        // The whole point: the delivery must be seen.
        assertThat(pillars.promiseKeeping())
                .as("handover must count even though it is the 5th event of a capped interaction")
                .isEqualTo(1.0);
        assertThat(pillars.responsiveness()).isGreaterThan(0.0);
    }

    @Test
    void oneCustomerCannotExceedTheInteractionCap() {
        UUID merchant = UUID.randomUUID();
        UUID customer = UUID.randomUUID();
        Instant now = Instant.now();

        // Ten separate requests from the same person.
        var rows = new java.util.ArrayList<ReservationEventRepository.CustomerEventRow>();
        for (int i = 0; i < 10; i++) {
            UUID basket = UUID.randomUUID();
            rows.add(new Row(customer, basket, "REQUEST_BROADCAST", now.minusSeconds(100L * i + 50)));
            rows.add(new Row(customer, basket, "HANDOVER_COMPLETED", now.minusSeconds(100L * i)));
        }

        var repo = mock(ReservationEventRepository.class);
        when(repo.findScoreableEvents(any(), any())).thenReturn(rows);
        when(repo.medianResponseSeconds(any(), any())).thenReturn(120.0);
        when(repo.distinctCompletedCustomers(any(), any())).thenReturn(1L);

        var weights = new BharosaWeights();
        var pillars = new BharosaEngine(repo, null, weights).computePillars(merchant);

        // Confidence is capped, so a single enthusiastic friend cannot
        // manufacture the evidence base for a high score.
        assertThat(pillars.confidence())
                .isEqualTo((double) weights.getPerCustomerCap());
    }
}
