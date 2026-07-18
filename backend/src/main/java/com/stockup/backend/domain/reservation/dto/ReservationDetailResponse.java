package com.stockup.backend.domain.reservation.dto;

import com.stockup.backend.domain.reservation.model.ReservationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationDetailResponse(
        UUID id,
        String storeName,
        ReservationStatus status,
        Instant reservedUntil,
        Instant createdAt,
        List<ReservedItemResponse> items
) {
}
