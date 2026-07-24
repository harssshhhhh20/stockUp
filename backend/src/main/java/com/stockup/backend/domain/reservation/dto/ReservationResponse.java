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

        ReservationStatus status,

        Instant reservedAt,

        Instant activeAt,

        Instant notificationSentAt,

        Instant viewedAt

) {
}
