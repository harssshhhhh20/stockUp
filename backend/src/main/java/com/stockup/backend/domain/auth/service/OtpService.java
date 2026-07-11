package com.stockup.backend.domain.auth.service;

public interface OtpService {

    void generateOtp(String email);

    void verifyOtp(String email, String otp);

}