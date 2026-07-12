package com.stockup.backend.infrastructure.notification.email.service;

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