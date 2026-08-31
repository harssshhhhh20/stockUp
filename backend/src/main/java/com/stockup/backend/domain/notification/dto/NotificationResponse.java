package com.stockup.backend.domain.notification.dto;

import com.stockup.backend.domain.notification.entity.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(

        UUID id,

        NotificationType type,

        String title,

        String message,

        UUID referenceId,

        boolean read,

        Instant readAt,

        Instant createdAt

) {
}
