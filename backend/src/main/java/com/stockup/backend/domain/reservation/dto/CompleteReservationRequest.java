package com.stockup.backend.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CompleteReservationRequest(

        @NotBlank(message = "OTP is required.")
        @Pattern(
                regexp = "\\d{6}",
                message = "OTP must be exactly 6 digits."
        )
        String otp

) {
}