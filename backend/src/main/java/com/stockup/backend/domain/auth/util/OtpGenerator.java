package com.stockup.backend.domain.auth.util;

import com.stockup.backend.common.config.properties.AppProperties;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpGenerator {

    private final SecureRandom secureRandom = new SecureRandom();
    private final AppProperties appProperties;

    public OtpGenerator(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String generate() {

        int length = appProperties.getOtp().getLength();

        int bound = (int) Math.pow(10, length);
        int lowerBound = bound / 10;

        int otp = secureRandom.nextInt(lowerBound, bound);

        return String.valueOf(otp);
    }
}