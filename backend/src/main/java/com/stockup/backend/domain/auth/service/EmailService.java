package com.stockup.backend.domain.auth.service;

public interface EmailService {

    void sendOtp(String email, String otp);

}