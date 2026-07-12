package com.stockup.backend.infrastructure.notification.email.provider.brevo.dto;

import java.util.List;

public record SendEmailRequest(

        SenderDto sender,

        List<RecipientDto> to,

        String subject,

        String htmlContent

) {
}