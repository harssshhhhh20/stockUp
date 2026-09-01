package com.stockup.backend.domain.auth.service.impl;

import com.stockup.backend.domain.auth.exception.InvalidOtpException;
import com.stockup.backend.domain.auth.exception.OtpExpiredException;
import com.stockup.backend.domain.auth.service.OtpRateLimiter;
import com.stockup.backend.infrastructure.notification.email.service.EmailService;
import com.stockup.backend.domain.auth.service.OtpService;
import com.stockup.backend.domain.auth.service.OtpStore;
import com.stockup.backend.domain.auth.util.OtpGenerator;
import org.springframework.stereotype.Service;

@Service
public class OtpServiceImpl implements OtpService {

    private final OtpStore otpStore;
    private final OtpGenerator otpGenerator;
    private final EmailService emailService;
    private final OtpRateLimiter otpRateLimiter;

    public OtpServiceImpl(
            OtpStore otpStore,
            OtpGenerator otpGenerator,
            EmailService emailService,
            OtpRateLimiter otpRateLimiter
    ) {
        this.otpStore = otpStore;
        this.otpGenerator = otpGenerator;
        this.emailService = emailService;
        this.otpRateLimiter = otpRateLimiter;
    }

    @Override
    public void generateOtp(String email) {

        otpRateLimiter.checkAndRecord(email);

        String otp = otpGenerator.generate();

        otpStore.save(email, otp);

        emailService.sendOtp(email, otp);
    }

    @Override
    public void verifyOtp(String email, String otp) {

        String storedOtp = otpStore.get(email)
                .orElseThrow(OtpExpiredException::new);

        if (!storedOtp.equals(otp)) {
            throw new InvalidOtpException();
        }

        otpStore.delete(email);
        otpRateLimiter.clear(email);
    }
}
