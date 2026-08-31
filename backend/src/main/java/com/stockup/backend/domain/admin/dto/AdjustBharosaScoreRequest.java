package com.stockup.backend.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdjustBharosaScoreRequest(

        @NotNull(message = "Delta is required.")
        Integer delta,

        @NotBlank(message = "Reason is required.")
        String reason

) {
}
