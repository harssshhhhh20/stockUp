package com.stockup.backend.infrastructure.notification.email.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "stockup.email",
        name = "provider",
        havingValue = "console",
        matchIfMissing = true
)
public class ConsoleEmailProvider implements EmailProvider {

    @Override
    public void send(String to, String subject, String body) {

        System.out.println("""
                
                ================================
                EMAIL
                ================================
                To      : %s
                Subject : %s

                %s
                ================================
                """.formatted(to, subject, body));
    }
}