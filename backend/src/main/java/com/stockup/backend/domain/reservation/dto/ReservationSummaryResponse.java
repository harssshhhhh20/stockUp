package com.stockup.backend.domain.reservation.dto;

import com.stockup.backend.domain.reservation.model.ReservationStatus;

import java.time.Instant;
import java.util.UUID;

public record ReservationSummaryResponse(
        UUID id,

        String storeName,

        ReservationStatus status,

        Instant reservedUntil,

        Instant createdAt
) {
}
