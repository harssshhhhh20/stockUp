package com.stockup.backend.domain.reservation.event;

import com.stockup.backend.domain.reservation.entity.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * The single way anything gets written to the event log.
 *
 * Recording is deliberately failure-tolerant: losing an analytics row must never
 * roll back a customer's actual order. A missing event costs a little accuracy
 * in a score; a failed handover costs someone their groceries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationEventRecorder {

    private final ReservationEventRepository repository;

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void record(
            UUID reservationId,
            UUID basketId,
            UUID storeId,
            UUID merchantId,
            ReservationEventType type,
            EventActor actor,
            String metadata
    ) {
        try {
            repository.save(ReservationEvent.of(
                    reservationId, basketId, storeId, merchantId,
                    type, actor, Instant.now(), metadata
            ));
        } catch (Exception ex) {
            log.warn("Could not record {} for basket {}: {}", type, basketId, ex.getMessage());
        }
    }

    /** Convenience for the funnel stages that happen before a reservation exists. */
    public void recordBroadcastStage(
            UUID basketId, UUID storeId, UUID merchantId,
            ReservationEventType type, EventActor actor
    ) {
        record(null, basketId, storeId, merchantId, type, actor, null);
    }

    /** Convenience for anything attached to a live reservation. */
    public void recordReservationStage(
            Reservation reservation, ReservationEventType type, EventActor actor, String metadata
    ) {
        record(
                reservation.getId(),
                reservation.getBasket().getId(),
                reservation.getStore().getId(),
                reservation.getMerchant().getId(),
                type, actor, metadata
        );
    }
}
