package com.stockup.backend.infrastructure.notification.email.service;

public interface EmailService {

    void sendOtp(String email, String otp);

    /**
     * Fire-and-forget transactional email (reservation updates and the like).
     * Delivery failures are logged, never surfaced to the caller — a shopper
     * should not see their reservation fail because an email bounced.
     */
    void sendNotification(String email, String subject, String body);

}
