package com.stockup.backend.domain.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Stars carry sentiment; the three chips carry the *reason*, and each maps onto
 * one Bharosa pillar so feedback can corroborate observed behaviour.
 */
public record SubmitFeedbackRequest(

        @NotNull(message = "A star rating is required.")
        @Min(value = 1, message = "Rating must be between 1 and 5.")
        @Max(value = 5, message = "Rating must be between 1 and 5.")
        Integer stars,

        Boolean repliedFast,
        Boolean readyOnTime,
        Boolean stockAccurate,

        @Size(max = 1000, message = "Comment cannot exceed 1000 characters.")
        String comment
) {
}
