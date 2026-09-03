package com.stockup.backend.infrastructure.notification.email.service;

/**
 * StockUp sends exactly one kind of email: the sign-in code. Anything else
 * belongs in the in-app feed or a push notification, so this interface stays
 * deliberately narrow.
 */
public interface EmailService {

    void sendOtp(String email, String otp);

}
