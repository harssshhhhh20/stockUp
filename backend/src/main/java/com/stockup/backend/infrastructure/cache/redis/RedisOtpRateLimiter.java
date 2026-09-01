package com.stockup.backend.infrastructure.cache.redis;

import com.stockup.backend.common.config.properties.AppProperties;
import com.stockup.backend.domain.auth.exception.OtpRateLimitedException;
import com.stockup.backend.domain.auth.service.OtpRateLimiter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Enforces the two OTP limits that were already configured under `app.otp`
 * but never applied: a cooldown between requests, and a cap per hour.
 */
@Component
public class RedisOtpRateLimiter implements OtpRateLimiter {

    private static final String COOLDOWN_PREFIX = "otp:cooldown:";
    private static final String ATTEMPTS_PREFIX = "otp:attempts:";

    private final StringRedisTemplate redisTemplate;
    private final AppProperties appProperties;

    public RedisOtpRateLimiter(
            StringRedisTemplate redisTemplate,
            AppProperties appProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.appProperties = appProperties;
    }

    @Override
    public void checkAndRecord(String email) {

        long resendWaitSeconds = appProperties.getOtp().getResendWaitSeconds();
        int maxAttemptsPerHour = appProperties.getOtp().getMaxAttemptsPerHour();

        if (resendWaitSeconds > 0
                && Boolean.TRUE.equals(redisTemplate.hasKey(COOLDOWN_PREFIX + email))) {

            Long ttl = redisTemplate.getExpire(COOLDOWN_PREFIX + email);
            long wait = ttl == null || ttl < 0 ? resendWaitSeconds : ttl;

            throw new OtpRateLimitedException(
                    "Please wait " + wait + " seconds before requesting another code."
            );
        }

        if (maxAttemptsPerHour > 0) {
            String attemptsKey = ATTEMPTS_PREFIX + email;
            Long attempts = redisTemplate.opsForValue().increment(attemptsKey);

            if (attempts != null && attempts == 1L) {
                redisTemplate.expire(attemptsKey, Duration.ofHours(1));
            }

            if (attempts != null && attempts > maxAttemptsPerHour) {
                throw new OtpRateLimitedException(
                        "Too many codes requested. Try again in an hour."
                );
            }
        }

        if (resendWaitSeconds > 0) {
            redisTemplate.opsForValue().set(
                    COOLDOWN_PREFIX + email,
                    "1",
                    Duration.ofSeconds(resendWaitSeconds)
            );
        }
    }

    @Override
    public void clear(String email) {
        redisTemplate.delete(COOLDOWN_PREFIX + email);
        redisTemplate.delete(ATTEMPTS_PREFIX + email);
    }
}
