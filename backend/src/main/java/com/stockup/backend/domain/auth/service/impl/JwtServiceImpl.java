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

        String secret = appProperties.getJwt().getSecret();

        // A blank value here usually means JWT_SECRET is set to "" somewhere
        // (an empty entry in .env overrides the default rather than falling
        // back to it). Say so, instead of failing with a key-length error.
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "app.jwt.secret is empty. Remove the empty JWT_SECRET entry "
                            + "from your .env (comment it out to use the default), "
                            + "or set it to a long random value."
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
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
            String email = extractEmail(refreshToken);
            return generateAccessToken(email);
        }catch (Exception e){
            throw e;
        }
    }
}