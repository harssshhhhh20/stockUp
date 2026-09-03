package com.stockup.backend.domain.reservation.dto;

import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(

        UUID id,

        UUID basketId,

        UUID merchantOfferId,

        UUID customerId,

        UUID merchantId,

        UUID storeId,

        // Enough for the customer to actually walk to the shop. A reservation
        // the shopper can't navigate to is only half an answer.
        String storeName,

        String storeAddress,

        Double storeLatitude,

        Double storeLongitude,

        ReservationStatus status,

        Instant reservedAt,

        Instant activeAt,

        Instant notificationSentAt,

        Instant viewedAt,

        /**
         * The pickup code, and only ever for the customer holding it. The
         * shopkeeper types this in to complete the handover, so returning it on
         * their side of the API would let them close an order with nobody
         * standing there.
         */
        String otp,

        /** When the customer can no longer call it off. Null once that passed. */
        Instant cancellableUntil,

        /** When a held order is released back to the shop. Null unless active. */
        Instant expiresAt

) {
}
