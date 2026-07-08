package com.stockup.backend.domain.auth.service;

public interface JwtService {

    String generateAccessToken(String phone);

    String generateRefreshToken(String phone);

    String extractPhone(String token);

    boolean isTokenValid(String token);

    String refreshAccessToken(String refreshToken);
}