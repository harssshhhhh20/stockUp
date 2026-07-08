package com.stockup.backend.domain.auth.controller;

import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.auth.dto.request.RefreshTokenRequest;
import com.stockup.backend.domain.auth.dto.request.RequestOtpRequest;
import com.stockup.backend.domain.auth.dto.request.VerifyOtpRequest;
import com.stockup.backend.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(
            @Valid @RequestBody RequestOtpRequest request
    ) {

        authService.requestOtp(request);

        return ApiResponseFactory.success(
                ResponseMessage.SUCCESS,
                "OTP sent successfully."
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {

        return ApiResponseFactory.success(
                ResponseMessage.SUCCESS,
                authService.verifyOtp(request)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {

        return ApiResponseFactory.success(
                ResponseMessage.SUCCESS,
                authentication.getName()
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ApiResponseFactory.success(
                ResponseMessage.SUCCESS,
                authService.refreshAccessToken(request)
        );
    }
}