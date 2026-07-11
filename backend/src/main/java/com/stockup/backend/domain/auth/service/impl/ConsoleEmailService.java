package com.stockup.backend.domain.auth.service.impl;

import com.stockup.backend.domain.auth.service.EmailService;
import org.springframework.stereotype.Service;

@Service
public class ConsoleEmailService implements EmailService {

    @Override
    public void sendOtp(String email, String otp) {

        System.out.println("""
                
                ================================
                EMAIL OTP
                ================================
                To   : %s
                OTP  : %s
                ================================
                """.formatted(email, otp));
    }
}