package com.stockup.backend.domain.notification.service;

import com.stockup.backend.domain.notification.dto.NotificationResponse;
import com.stockup.backend.domain.notification.entity.enums.NotificationType;
import com.stockup.backend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    void notify(
            User recipient,
            NotificationType type,
            String title,
            String message,
            UUID referenceId
    );

    Page<NotificationResponse> getMyNotifications(Pageable pageable);

    long getUnreadCount();

    NotificationResponse markRead(UUID notificationId);
}
