package com.stockup.backend.domain.reservation.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReservationEventRepository extends JpaRepository<ReservationEvent, UUID> {

    /** The timeline for one order. */
    List<ReservationEvent> findByReservationIdOrderByOccurredAtAsc(UUID reservationId);

    List<ReservationEvent> findByBasketIdOrderByOccurredAtAsc(UUID basketId);

    /** Everything a shop did since a cutoff — the scoring hot path. */
    List<ReservationEvent> findByMerchantIdAndOccurredAtAfterOrderByOccurredAtDesc(
            UUID merchantId, Instant since);

    List<ReservationEvent> findByMerchantIdAndEventTypeAndOccurredAtAfter(
            UUID merchantId, ReservationEventType eventType, Instant since);

    long countByMerchantIdAndEventTypeAndOccurredAtAfter(
            UUID merchantId, ReservationEventType eventType, Instant since);

    /**
     * Median response latency in seconds. Computed in SQL because pulling every
     * event into memory to sort them would not survive real volume.
     */
    @Query(value = """
            SELECT percentile_cont(0.5) WITHIN GROUP (
                       ORDER BY EXTRACT(EPOCH FROM (o.occurred_at - b.occurred_at)))
            FROM reservation_events o
            JOIN reservation_events b
              ON b.basket_id = o.basket_id
             AND b.merchant_id = o.merchant_id
             AND b.event_type = 'REQUEST_BROADCAST'
            WHERE o.merchant_id = :merchantId
              AND o.event_type = 'OFFER_SUBMITTED'
              AND o.occurred_at > :since
            """, nativeQuery = true)
    Double medianResponseSeconds(@Param("merchantId") UUID merchantId,
                                 @Param("since") Instant since);

    /**
     * Every scoreable event for a shop, tagged with the customer it belongs to.
     *
     * The customer comes from the basket rather than the reservation, because the
     * early funnel events (broadcast, view, offer) happen before any reservation
     * exists — and those are exactly the events that reveal whether a shop bothers
     * to answer at all.
     */
    @Query(value = """
            SELECT b.customer_id AS customerId,
                   e.basket_id   AS basketId,
                   e.event_type  AS eventType,
                   e.occurred_at AS occurredAt
            FROM reservation_events e
            JOIN baskets b ON b.id = e.basket_id
            WHERE e.merchant_id = :merchantId
              AND e.occurred_at > :since
            ORDER BY e.occurred_at
            """, nativeQuery = true)
    List<CustomerEventRow> findScoreableEvents(@Param("merchantId") UUID merchantId,
                                               @Param("since") Instant since);

    interface CustomerEventRow {
        UUID getCustomerId();
        UUID getBasketId();
        String getEventType();
        Instant getOccurredAt();
    }

    /**
     * Distinct customers this shop has actually served — the breadth signal that
     * stops one enthusiastic friend from manufacturing a reputation.
     */
    @Query(value = """
            SELECT COUNT(DISTINCT r.customer_id)
            FROM reservation_events e
            JOIN reservations r ON r.id = e.reservation_id
            WHERE e.merchant_id = :merchantId
              AND e.event_type = 'HANDOVER_COMPLETED'
              AND e.occurred_at > :since
            """, nativeQuery = true)
    long distinctCompletedCustomers(@Param("merchantId") UUID merchantId,
                                    @Param("since") Instant since);
}
