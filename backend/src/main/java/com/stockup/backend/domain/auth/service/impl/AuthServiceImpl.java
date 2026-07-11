package com.stockup.backend.domain.auth.service.impl;

import com.stockup.backend.domain.auth.dto.request.RefreshTokenRequest;
import com.stockup.backend.domain.auth.dto.request.RequestOtpRequest;
import com.stockup.backend.domain.auth.dto.request.VerifyOtpRequest;
import com.stockup.backend.domain.auth.dto.response.AuthResponse;
import com.stockup.backend.domain.auth.service.AuthService;
import com.stockup.backend.domain.auth.service.JwtService;
import com.stockup.backend.domain.auth.service.OtpService;
import com.stockup.backend.domain.user.entity.User;
import com.stockup.backend.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthServiceImpl(
            OtpService otpService,
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void requestOtp(RequestOtpRequest request) {
        otpService.generateOtp(request.email());
    }

    @Override
    public AuthResponse verifyOtp(VerifyOtpRequest request) {

        otpService.verifyOtp(
                request.email(),
                request.otp()
        );

        User user = userRepository
                .findByEmail(request.email())
                .orElse(null);

        boolean newUser = false;

        if (user == null) {

            user = new User(
                    null,
                    null,
                    request.email(),
                    null
            );

            user.verify();

            userRepository.save(user);

            newUser = true;
        }

        return new AuthResponse(
                jwtService.generateAccessToken(user.getPhone()),
                jwtService.generateRefreshToken(user.getPhone()),
                newUser
        );
    }

    @Override
    public String refreshAccessToken(RefreshTokenRequest request){
        return jwtService.refreshAccessToken(request.refreshToken());
    }
}