package com.stockup.backend.domain.auth.service;

public interface JwtService {

    String generateAccessToken(String email);

    String generateRefreshToken(String email);

    String extractEmail(String email);

    boolean isTokenValid(String token);

    String refreshAccessToken(String refreshToken);
}