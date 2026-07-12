package com.stockup.backend.infrastructure.notification.email.provider.brevo.dto;

public record SenderDto(
        String name,
        String email
) {
}