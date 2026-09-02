package com.stockup.backend.domain.feedback.dto;

import java.util.List;

/**
 * What a store page shows: an average, and — more usefully — what customers
 * actually said about *why*.
 */
public record StoreFeedbackSummary(
        Double averageStars,
        long totalReviews,
        /** e.g. "⚡ Replies quickly" — derived from chip agreement, not typed by anyone. */
        List<String> commonlySaid,
        List<FeedbackResponse> recent
) {
}
