package com.stockup.backend.domain.auth.service.impl;

import com.stockup.backend.common.config.properties.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {

        AppProperties properties = new AppProperties();

        properties.getJwt().setIssuer("stockup");
        properties.getJwt().setSecret("this-is-a-very-long-secret-key-for-testing-only-please-change");
        properties.getJwt().setAccessTokenExpirySeconds(900);
        properties.getJwt().setRefreshTokenExpirySeconds(604800);

        jwtService = new JwtServiceImpl(properties);
    }

    @Test
    void shouldGenerateValidAccessToken() {

        String token = jwtService.generateAccessToken("+919876543210");

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals("+919876543210", jwtService.extractPhone(token));
    }

    @Test
    void shouldRejectInvalidToken() {

        String invalidToken = "invalid.jwt.token";

        assertFalse(jwtService.isTokenValid(invalidToken));
    }
}