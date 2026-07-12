package com.stockup.backend.infrastructure.notification.email.template;

import org.springframework.stereotype.Component;

@Component
public class OtpEmailTemplate {

    public String subject() {
        return "StockUp OTP Verification";
    }

    public String body(String otp) {
        return """
                Your OTP for StockUp is: %s

                This OTP is valid for 5 minutes.

                If you didn't request this OTP, you can safely ignore this email.
                """.formatted(otp);
    }
}