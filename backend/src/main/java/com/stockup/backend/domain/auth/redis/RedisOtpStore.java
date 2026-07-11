package com.stockup.backend.domain.auth.redis;

import com.stockup.backend.common.config.properties.AppProperties;
import com.stockup.backend.domain.auth.service.OtpStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisOtpStore implements OtpStore {

    private static final String OTP_PREFIX = "otp:email:";
    private final StringRedisTemplate redisTemplate;
    private final AppProperties appProperties;

    public RedisOtpStore(
            StringRedisTemplate redisTemplate,
            AppProperties appProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.appProperties = appProperties;
    }

    @Override
    public void save(String email, String otp) {

        redisTemplate.opsForValue().set(
                key(email),
                otp,
                Duration.ofSeconds(appProperties.getOtp().getExpirySeconds())
        );
    }

    @Override
    public Optional<String> get(String email) {

        return Optional.ofNullable(
                redisTemplate.opsForValue().get(key(email))
        );
    }

    @Override
    public void delete(String email) {

        redisTemplate.delete(key(email));
    }

    @Override
    public boolean exists(String email) {

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(key(email))
        );
    }

    private String key(String email) {
        return OTP_PREFIX + email;
    }
}