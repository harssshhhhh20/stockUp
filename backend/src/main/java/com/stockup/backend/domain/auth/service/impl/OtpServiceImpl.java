package com.stockup.backend.domain.auth.service.impl;

import com.stockup.backend.infrastructure.notification.email.service.EmailService;
import com.stockup.backend.domain.auth.service.OtpService;
import com.stockup.backend.domain.auth.service.OtpStore;
import com.stockup.backend.domain.auth.util.OtpGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OtpServiceImpl implements OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpServiceImpl.class);

    private final OtpStore otpStore;
    private final OtpGenerator otpGenerator;
    private final EmailService emailService;

    public OtpServiceImpl(
            OtpStore otpStore,
            OtpGenerator otpGenerator,
            EmailService emailService
    ) {
        this.otpStore = otpStore;
        this.otpGenerator = otpGenerator;
        this.emailService = emailService;
    }

    @Override
    public void generateOtp(String email) {

        String otp = otpGenerator.generate();

        otpStore.save(email, otp);

        emailService.sendOtp(email, otp);
    }

    @Override
    public void verifyOtp(String email, String otp) {

        String storedOtp = otpStore.get(email)
                .orElseThrow(() -> new RuntimeException("OTP expired"));

        if (!storedOtp.equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        otpStore.delete(email);
    }
}