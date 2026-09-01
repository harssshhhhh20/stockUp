package com.stockup.backend.domain.auth.service;

public interface OtpRateLimiter {

    /**
     * Throws if this email has requested a code too recently, or too many times
     * in the last hour. Records the request when it is allowed.
     */
    void checkAndRecord(String email);

    void clear(String email);
}
