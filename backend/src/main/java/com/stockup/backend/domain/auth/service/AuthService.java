package com.stockup.backend.domain.auth.service;

import com.stockup.backend.domain.auth.dto.request.RefreshTokenRequest;
import com.stockup.backend.domain.auth.dto.request.RequestOtpRequest;
import com.stockup.backend.domain.auth.dto.request.VerifyOtpRequest;
import com.stockup.backend.domain.auth.dto.response.AuthResponse;

public interface AuthService {

    void requestOtp(RequestOtpRequest request);

    AuthResponse verifyOtp(VerifyOtpRequest request);

    String refreshAccessToken(RefreshTokenRequest request);

}