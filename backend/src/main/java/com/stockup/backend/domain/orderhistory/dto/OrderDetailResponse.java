package com.stockup.backend.domain.orderhistory.dto;

import com.stockup.backend.domain.feedback.dto.FeedbackResponse;
import com.stockup.backend.domain.reservation.entity.enums.ReservationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        UUID reservationId,
        UUID basketId,
        UUID storeId,
        String storeName,
        ReservationStatus status,
        Instant reservedAt,
        Instant activeAt,
        List<String> items,
        /** Seconds from request reaching the shop to the shop answering. */
        Long responseSeconds,
        /** Seconds from reserving to handover. */
        Long fulfilmentSeconds,
        String cancellationReason,
        List<OrderTimelineEntry> timeline,
        FeedbackResponse feedback,
        boolean canRate
) {
}
