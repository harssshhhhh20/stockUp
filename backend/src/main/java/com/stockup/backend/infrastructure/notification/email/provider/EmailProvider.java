package com.stockup.backend.infrastructure.notification.email.provider;

public interface EmailProvider {

    void send(
            String to,
            String subject,
            String body
    );
}