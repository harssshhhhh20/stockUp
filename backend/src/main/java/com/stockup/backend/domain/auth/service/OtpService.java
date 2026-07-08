package com.stockup.backend.domain.auth.service;

public interface OtpService {

    void generateOtp(String phone);

    void verifyOtp(String phone, String otp);

}