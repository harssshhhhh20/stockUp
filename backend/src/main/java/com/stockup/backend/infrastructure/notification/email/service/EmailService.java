package com.stockup.backend.infrastructure.notification.email.service;

public interface EmailService {

    void sendOtp(String email, String otp);

}