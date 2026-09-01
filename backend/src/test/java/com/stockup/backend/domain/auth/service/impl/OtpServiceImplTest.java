package com.stockup.backend.domain.auth.service.impl;

import com.stockup.backend.domain.auth.exception.InvalidOtpException;
import com.stockup.backend.domain.auth.exception.OtpExpiredException;
import com.stockup.backend.domain.auth.service.OtpRateLimiter;
import com.stockup.backend.domain.auth.service.OtpStore;
import com.stockup.backend.domain.auth.util.OtpGenerator;
import com.stockup.backend.infrastructure.notification.email.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OtpServiceImplTest {

    private Map<String, String> store;
    private OtpServiceImpl otpService;
    private String lastSentTo;
    private String lastSentOtp;

    @BeforeEach
    void setUp() {
        store = new HashMap<>();
        lastSentTo = null;
        lastSentOtp = null;

        OtpStore otpStore = new OtpStore() {
            @Override
            public void save(String email, String otp) {
                store.put(email, otp);
            }

            @Override
            public Optional<String> get(String email) {
                return Optional.ofNullable(store.get(email));
            }

            @Override
            public void delete(String email) {
                store.remove(email);
            }

            @Override
            public boolean exists(String email) {
                return store.containsKey(email);
            }
        };

        OtpGenerator generator = mock(OtpGenerator.class);
        when(generator.generate()).thenReturn("123456");

        EmailService emailService = new EmailService() {
            @Override
            public void sendOtp(String email, String otp) {
                lastSentTo = email;
                lastSentOtp = otp;
            }

            @Override
            public void sendNotification(String email, String subject, String body) {
            }
        };

        OtpRateLimiter noopLimiter = new OtpRateLimiter() {
            @Override
            public void checkAndRecord(String email) {
            }

            @Override
            public void clear(String email) {
            }
        };

        otpService = new OtpServiceImpl(otpStore, generator, emailService, noopLimiter);
    }

    @Test
    void generatesStoresAndEmailsTheCode() {
        otpService.generateOtp("shopper@example.com");

        assertThat(store).containsEntry("shopper@example.com", "123456");
        assertThat(lastSentTo).isEqualTo("shopper@example.com");
        assertThat(lastSentOtp).isEqualTo("123456");
    }

    @Test
    void acceptsTheCorrectCodeAndConsumesIt() {
        otpService.generateOtp("shopper@example.com");

        otpService.verifyOtp("shopper@example.com", "123456");

        // Single use: the code must not survive a successful verification.
        assertThat(store).doesNotContainKey("shopper@example.com");
    }

    @Test
    void aWrongCodeIsUnauthorizedNotAServerError() {
        otpService.generateOtp("shopper@example.com");

        assertThatThrownBy(() -> otpService.verifyOtp("shopper@example.com", "000000"))
                .isInstanceOf(InvalidOtpException.class)
                .extracting(e -> ((InvalidOtpException) e).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void anExpiredOrUnknownCodeIsUnauthorizedNotAServerError() {
        assertThatThrownBy(() -> otpService.verifyOtp("nobody@example.com", "123456"))
                .isInstanceOf(OtpExpiredException.class)
                .extracting(e -> ((OtpExpiredException) e).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void aWrongAttemptDoesNotConsumeTheCode() {
        otpService.generateOtp("shopper@example.com");

        assertThatThrownBy(() -> otpService.verifyOtp("shopper@example.com", "999999"))
                .isInstanceOf(InvalidOtpException.class);

        // A typo must not lock the user out of their own valid code.
        otpService.verifyOtp("shopper@example.com", "123456");
        assertThat(store).doesNotContainKey("shopper@example.com");
    }
}
