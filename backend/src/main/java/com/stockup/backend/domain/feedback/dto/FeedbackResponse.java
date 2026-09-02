package com.stockup.backend.domain.feedback.dto;

import java.time.Instant;
import java.util.UUID;

public record FeedbackResponse(
        UUID id,
        UUID reservationId,
        int stars,
        Boolean repliedFast,
        Boolean readyOnTime,
        Boolean stockAccurate,
        String comment,
        /** Always true — unverified feedback cannot exist in this system. */
        boolean verifiedPurchase,
        /** First name only; reviews are public, identities are not. */
        String reviewerName,
        Instant createdAt
) {
}
