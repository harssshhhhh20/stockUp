package com.stockup.backend.domain.orderhistory.dto;

import java.time.Instant;

/** One row of an order's timeline, already phrased for a person to read. */
public record OrderTimelineEntry(
        String eventType,
        String label,
        String actor,
        Instant occurredAt
) {
}
