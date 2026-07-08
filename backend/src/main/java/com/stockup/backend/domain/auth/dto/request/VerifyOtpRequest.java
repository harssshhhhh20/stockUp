package com.stockup.backend.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(

        @NotBlank(message = "Phone number is required.")
        @Pattern(
                regexp = "^\\+[1-9]\\d{7,14}$",
                message = "Phone number must be in E.164 format."
        )
        String phone,

        @NotBlank(message = "OTP is required.")
        @Pattern(
                regexp = "^\\d{6}$",
                message = "OTP must contain exactly 6 digits."
        )
        String otp

) {
}