package com.stockup.backend.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Otp otp = new Otp();
    private final Jwt jwt = new Jwt();

    public Otp getOtp() {
        return otp;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public static class Otp {

        private int length;
        private long expirySeconds;
        private long resendWaitSeconds;
        private int maxAttemptsPerHour;

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public long getExpirySeconds() {
            return expirySeconds;
        }

        public void setExpirySeconds(long expirySeconds) {
            this.expirySeconds = expirySeconds;
        }

        public long getResendWaitSeconds() {
            return resendWaitSeconds;
        }

        public void setResendWaitSeconds(long resendWaitSeconds) {
            this.resendWaitSeconds = resendWaitSeconds;
        }

        public int getMaxAttemptsPerHour() {
            return maxAttemptsPerHour;
        }

        public void setMaxAttemptsPerHour(int maxAttemptsPerHour) {
            this.maxAttemptsPerHour = maxAttemptsPerHour;
        }
    }

    public static class Jwt {

        private String issuer;
        private long accessTokenExpirySeconds;
        private long refreshTokenExpirySeconds;
        private String secret;

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public long getAccessTokenExpirySeconds() {
            return accessTokenExpirySeconds;
        }

        public void setAccessTokenExpirySeconds(long accessTokenExpirySeconds) {
            this.accessTokenExpirySeconds = accessTokenExpirySeconds;
        }

        public long getRefreshTokenExpirySeconds() {
            return refreshTokenExpirySeconds;
        }

        public void setRefreshTokenExpirySeconds(long refreshTokenExpirySeconds) {
            this.refreshTokenExpirySeconds = refreshTokenExpirySeconds;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }
}