package com.stockup.backend.domain.auth.service.impl;

import com.stockup.backend.common.config.properties.AppProperties;
import com.stockup.backend.domain.auth.exception.InvalidTokenException;
import com.stockup.backend.domain.auth.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    private final AppProperties appProperties;
    private final SecretKey secretKey;

    public JwtServiceImpl(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.secretKey = Keys.hmacShaKeyFor(
                appProperties.getJwt()
                        .getSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public String generateAccessToken(String email) {

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(email)
                .issuer(appProperties.getJwt().getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(
                        now.plusSeconds(
                                appProperties.getJwt()
                                        .getAccessTokenExpirySeconds()
                        )
                ))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String generateRefreshToken(String email) {

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(email)
                .issuer(appProperties.getJwt().getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(
                        now.plusSeconds(
                                appProperties.getJwt()
                                        .getRefreshTokenExpirySeconds()
                        )
                ))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String extractEmail(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    @Override
    public boolean isTokenValid(String token) {

        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public String refreshAccessToken(String refreshToken){
        try{
            if (!isTokenValid(refreshToken)) {
                throw new InvalidTokenException();
            }
            String phone = extractEmail(refreshToken);
            return generateAccessToken(phone);
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }
}