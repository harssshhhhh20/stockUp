package com.stockup.backend.domain.auth.service.impl;

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

    public OtpServiceImpl(
            OtpStore otpStore,
            OtpGenerator otpGenerator
    ) {
        this.otpStore = otpStore;
        this.otpGenerator = otpGenerator;
    }

    @Override
    public void generateOtp(String phone) {

        String otp = otpGenerator.generate();

        otpStore.save(phone, otp);

        log.info("OTP for {} is {}", phone, otp);
    }

    @Override
    public void verifyOtp(String phone, String otp) {

        String storedOtp = otpStore.get(phone)
                .orElseThrow(() -> new RuntimeException("OTP expired"));

        if (!storedOtp.equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        otpStore.delete(phone);
    }
}