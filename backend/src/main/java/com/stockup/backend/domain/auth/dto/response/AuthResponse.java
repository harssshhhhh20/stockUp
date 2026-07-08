package com.stockup.backend.domain.auth.dto.response;

public record AuthResponse(

        String accessToken,

        String refreshToken,

        boolean newUser

) {
}