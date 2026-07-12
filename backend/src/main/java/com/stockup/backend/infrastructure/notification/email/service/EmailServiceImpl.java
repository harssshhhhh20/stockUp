package com.stockup.backend.infrastructure.notification.email.service;

import com.stockup.backend.infrastructure.notification.email.provider.EmailProvider;
import com.stockup.backend.infrastructure.notification.email.template.OtpEmailTemplate;
import org.springframework.stereotype.Service;

@Service
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

        emailProvider.send(email, subject, body);
    }
}