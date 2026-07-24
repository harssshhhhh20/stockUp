package com.stockup.backend.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelReservationRequest(

        @NotBlank(message = "Cancellation reason is required.")
        @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters.")
        String reason

) {
}