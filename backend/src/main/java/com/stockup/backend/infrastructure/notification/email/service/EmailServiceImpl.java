package com.stockup.backend.infrastructure.notification.email.service;

import com.stockup.backend.infrastructure.notification.email.provider.EmailProvider;
import com.stockup.backend.infrastructure.notification.email.template.OtpEmailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final EmailProvider emailProvider;
    private final OtpEmailTemplate otpEmailTemplate;

    public EmailServiceImpl(
            EmailProvider emailProvider,
            OtpEmailTemplate otpEmailTemplate
    ) {
        this.emailProvider = emailProvider;
        this.otpEmailTemplate = otpEmailTemplate;
    }

    @Override
    public void sendOtp(String email, String otp) {

        String subject = otpEmailTemplate.subject();
        String body = otpEmailTemplate.body(otp);

        // Deliberately synchronous: if the code can't be delivered the caller
        // needs to know, because there is nothing else to sign in with.
        emailProvider.send(email, subject, body);
    }

    @Override
    @Async
    public void sendNotification(String email, String subject, String body) {
        try {
            emailProvider.send(email, subject, body);
        } catch (Exception ex) {
            log.warn("Failed to send notification email to {}: {}", email, ex.getMessage());
        }
    }
}
