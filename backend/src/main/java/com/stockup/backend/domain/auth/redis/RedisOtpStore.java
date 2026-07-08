package com.stockup.backend.domain.auth.redis;

import com.stockup.backend.common.config.properties.AppProperties;
import com.stockup.backend.domain.auth.service.OtpStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisOtpStore implements OtpStore {

    private static final String OTP_PREFIX = "otp:";

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
    public void save(String phone, String otp) {

        redisTemplate.opsForValue().set(
                key(phone),
                otp,
                Duration.ofSeconds(appProperties.getOtp().getExpirySeconds())
        );
    }

    @Override
    public Optional<String> get(String phone) {

        return Optional.ofNullable(
                redisTemplate.opsForValue().get(key(phone))
        );
    }

    @Override
    public void delete(String phone) {

        redisTemplate.delete(key(phone));
    }

    @Override
    public boolean exists(String phone) {

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(key(phone))
        );
    }

    private String key(String phone) {
        return OTP_PREFIX + phone;
    }
}