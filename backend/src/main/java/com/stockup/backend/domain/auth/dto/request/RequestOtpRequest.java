package com.stockup.backend.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestOtpRequest(

        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email address.")
        String email

) {
}