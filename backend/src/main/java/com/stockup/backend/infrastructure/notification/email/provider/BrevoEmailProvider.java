package com.stockup.backend.infrastructure.notification.email.provider;

import com.stockup.backend.common.config.properties.EmailProperties;
import com.stockup.backend.infrastructure.notification.email.exception.EmailDeliveryException;
import com.stockup.backend.infrastructure.notification.email.provider.brevo.dto.RecipientDto;
import com.stockup.backend.infrastructure.notification.email.provider.brevo.dto.SendEmailRequest;
import com.stockup.backend.infrastructure.notification.email.provider.brevo.dto.SenderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@ConditionalOnProperty(
        prefix = "stockup.email",
        name = "provider",
        havingValue = "brevo"
)
public class BrevoEmailProvider implements EmailProvider {

    private final RestClient restClient;
    private final EmailProperties emailProperties;

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailProvider.class);

    public BrevoEmailProvider(
            RestClient restClient,
            EmailProperties emailProperties
    ) {
        this.restClient = restClient;
        this.emailProperties = emailProperties;
    }

    @Override
    public void send(String to, String subject, String body) {

        SendEmailRequest request = new SendEmailRequest(
                new SenderDto(
                        emailProperties.getSenderName(),
                        emailProperties.getSenderEmail()
                ),
                List.of(new RecipientDto(to)),
                subject,
                "<pre>" + body + "</pre>"
        );

        try {
            restClient.post()
                    .uri(emailProperties.getBaseUrl() + "/v3/smtp/email")
                    .header("api-key", emailProperties.getApiKey())
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .body(request)
                    .retrieve()
                    .toEntity(String.class);

            log.info("Email sent successfully to {}", to);

        } catch (Exception ex) {

            log.error("Failed to send email to {}", to, ex);

            throw new EmailDeliveryException(
                    "Failed to deliver email using Brevo.",
                    ex
            );
        }


    }
}