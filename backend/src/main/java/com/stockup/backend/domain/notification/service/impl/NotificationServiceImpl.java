package com.stockup.backend.domain.notification.service.impl;

import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.notification.dto.NotificationResponse;
import com.stockup.backend.domain.notification.entity.Notification;
import com.stockup.backend.domain.notification.entity.enums.NotificationType;
import com.stockup.backend.domain.notification.exception.NotificationAccessDeniedException;
import com.stockup.backend.domain.notification.exception.NotificationNotFoundException;
import com.stockup.backend.domain.notification.mapper.NotificationMapper;
import com.stockup.backend.domain.notification.repository.NotificationRepository;
import com.stockup.backend.domain.notification.service.NotificationService;
import com.stockup.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final CurrentUserService currentUserService;

    @Override
    public void notify(
            User recipient,
            NotificationType type,
            String title,
            String message,
            UUID referenceId
    ) {
        Notification notification = Notification.create(
                recipient,
                type,
                title,
                message,
                referenceId
        );

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {

        User currentUser = currentUserService.getCurrentUser();

        return notificationRepository
                .findAllByRecipientOrderByCreatedAtDesc(currentUser, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User currentUser = currentUserService.getCurrentUser();
        return notificationRepository.countByRecipientAndReadFalse(currentUser);
    }

    @Override
    public NotificationResponse markRead(UUID notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found."
                ));

        User currentUser = currentUserService.getCurrentUser();

        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new NotificationAccessDeniedException(
                    "You are not authorized to access this notification."
            );
        }

        notification.markRead();

        return notificationMapper.toResponse(notification);
    }
}
