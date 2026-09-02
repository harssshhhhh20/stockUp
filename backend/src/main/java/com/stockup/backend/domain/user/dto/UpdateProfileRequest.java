package com.stockup.backend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @NotBlank(message = "Please tell us your name.")
        @Size(max = 100, message = "That name is too long.")
        String firstName,

        @Size(max = 100, message = "That name is too long.")
        String lastName,

        // Deliberately permissive: people write numbers with spaces, +91, and
        // dashes, and rejecting those teaches them nothing useful.
        @NotBlank(message = "A phone number is needed so shops can reach you.")
        @Pattern(regexp = "^[+0-9][0-9 \\-()]{6,19}$", message = "That doesn't look like a phone number.")
        String phone
) {
}
